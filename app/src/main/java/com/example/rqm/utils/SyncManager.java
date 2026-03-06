package com.example.rqm.utils;

import android.content.Context;
import android.text.TextUtils;

import com.example.rqm.data.MaterialCacheDao;
import com.example.rqm.data.RequisicaoDAO;
import com.example.rqm.data.ResponsavelDao;
import com.example.rqm.data.SyncRunDao;
import com.example.rqm.models.Material;
import com.example.rqm.models.Requisicao;
import com.example.rqm.models.SyncRun;

import java.util.List;
import java.util.UUID;

public class SyncManager {

    public static SyncRun executarSincronizacao(Context context) {
        String startedAt = DateUtils.toIsoWithNow("");
        SyncRun run = new SyncRun();
        run.syncUuid = UUID.randomUUID().toString();
        run.startedAt = startedAt;
        run.createdAt = startedAt;
        run.status = "RUNNING";
        run.deviceId = obterDeviceId(context);
        run.userId = AuthPrefs.getUserId(context);
        run.tenantId = AuthPrefs.getTenantId(context);
        run.source = "APP";
        run.message = "Sincronizacao em andamento.";

        RequisicaoDAO requisicaoDAO = new RequisicaoDAO(context);
        SyncRunDao syncRunDao = new SyncRunDao(context);

        if (AuthPrefs.isTestSession(context)) {
            run.finishedAt = DateUtils.toIsoWithNow("");
            run.status = "ERROR";
            run.message = "Modo teste nao envia dados para o servidor.";
            syncRunDao.salvar(run);
            return run;
        }

        if (!SupabasePrefs.hasConfig(context) || SupabasePrefs.getStatus(context) != SupabasePrefs.STATUS_CONNECTED) {
            run.finishedAt = DateUtils.toIsoWithNow("");
            run.status = "ERROR";
            run.message = "Banco de dados nao conectado.";
            syncRunDao.salvar(run);
            return run;
        }

        List<Requisicao> pendentes = requisicaoDAO.listarPendentesSync();
        run.pendingTotal = pendentes.size();
        int catalogosAtualizados = 0;

        for (Requisicao req : pendentes) {
            SupabaseUploader.UploadResult result = SupabaseUploader.enviarRequisicao(context, req);
            String syncTime = DateUtils.toIsoWithNow("");
            if (result.success) {
                requisicaoDAO.atualizarSyncStatus(req.id, RequisicaoDAO.SYNC_SYNCED, syncTime);
                run.pendingSent++;
                run.materialsUpdated += contarItens(req);
            } else if ("REJECTED".equalsIgnoreCase(result.status)) {
                requisicaoDAO.atualizarSyncStatus(req.id, RequisicaoDAO.SYNC_CONFLICT, syncTime);
                run.conflictsFound++;
            } else {
                requisicaoDAO.atualizarSyncStatus(req.id, RequisicaoDAO.SYNC_ERROR, syncTime);
                run.errorsCount++;
            }
        }

        catalogosAtualizados += sincronizarMateriais(context);
        catalogosAtualizados += sincronizarResponsaveis(context);

        run.finishedAt = DateUtils.toIsoWithNow("");
        if (run.errorsCount > 0 && run.pendingSent == 0) {
            run.status = "ERROR";
        } else if (run.conflictsFound > 0 || run.errorsCount > 0) {
            run.status = "PARTIAL";
        } else {
            run.status = "SUCCESS";
        }
        run.message = montarMensagem(run, catalogosAtualizados);

        syncRunDao.salvar(run);
        enviarSyncRunsPendentes(context, syncRunDao);
        return run;
    }

    private static int contarItens(Requisicao req) {
        List<Material> materiais = req.getMateriaisSelecionados();
        return materiais != null ? materiais.size() : 0;
    }

    private static String montarMensagem(SyncRun run, int catalogosAtualizados) {
        return run.pendingSent + " pendencias enviadas, "
                + run.materialsUpdated + " itens sincronizados, "
                + catalogosAtualizados + " cadastros baixados, "
                + run.conflictsFound + " conflitos, "
                + run.errorsCount + " erros.";
    }

    private static int sincronizarMateriais(Context context) {
        SupabaseEdgeClient.MaterialCatalogResult result = SupabaseEdgeClient.getMateriais(context);
        if (!result.success) {
            return 0;
        }
        new MaterialCacheDao(context).replaceAll(result.items);
        return result.items.size();
    }

    private static int sincronizarResponsaveis(Context context) {
        SupabaseEdgeClient.ResponsavelResult result = SupabaseEdgeClient.getResponsaveis(context);
        if (!result.success) {
            return 0;
        }
        new ResponsavelDao(context).replaceAll(result.items);
        return result.items.size();
    }

    public static void enviarSyncRunsPendentes(Context context, SyncRunDao syncRunDao) {
        if (!SupabasePrefs.hasConfig(context) || SupabasePrefs.getStatus(context) != SupabasePrefs.STATUS_CONNECTED) {
            return;
        }
        if (TextUtils.isEmpty(AuthPrefs.getAccessToken(context))) {
            return;
        }

        List<SyncRun> pendentes = syncRunDao.listarPendentesUpload();
        for (SyncRun run : pendentes) {
            SupabaseEdgeClient.SyncRunResult result = SupabaseEdgeClient.sendSyncRun(context, run);
            if (result.success) {
                syncRunDao.marcarComoEnviado(run.id);
            }
        }
    }

    private static String obterDeviceId(Context context) {
        String imei = AuthPrefs.getImei(context);
        return TextUtils.isEmpty(imei) ? "N/A" : imei;
    }
}
