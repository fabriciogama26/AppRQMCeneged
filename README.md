# RQM

Aplicativo Android para requisicao, devolucao, consulta de estoque e sincronizacao de materiais com backend em Supabase.

## Estado atual

### App Android
- Login por matricula + IMEI.
- Modo teste local.
- Home com operacoes retrateis.
- Fluxo completo de requisicao/devolucao:
  - formulario
  - materiais
  - confirmacao
  - salvamento local
  - envio ao backend quando conectado
  - exportacao local
- Historico local.
- Tela de estoque.
- Tela de sincronizacoes.
- Cache local de materiais e responsaveis.

### Backend Supabase
- Migrations de `000` a `015`.
- Edge Functions para login, PIN admin, logout, log de erro, sync, envio de movimentacao e consultas de saldo/catalogo.
- RPCs de materiais com controle de saldo fisico e saldo liquido por projeto.
- Padrao de auditoria com:
  - `created_by`
  - `updated_by`
  - `created_at`
  - `updated_at`

## Fluxo principal do app

1. Splash.
2. Login.
3. Home.
4. Operacao:
   - Requisicao
   - Devolucao
   - Estoque
5. Requisicao/Devolucao:
   - `FormularioOperacaoFragment`
   - `MateriaisFragment`
   - `ConfirmacaoFragment`
6. Salvamento local em SQLite.
7. Se conectado ao Supabase:
   - envia para `submit_material_request`
   - exporta localmente
8. Se nao conectado:
   - mantem pendente
   - exporta localmente
9. `Sincronizar agora` reenfileira e baixa cadastros base.

## Estrutura principal

### App
- `app/src/main/java/com/example/rqm/MainActivity.java`
- `app/src/main/java/com/example/rqm/ui/`
- `app/src/main/java/com/example/rqm/data/`
- `app/src/main/java/com/example/rqm/utils/`
- `app/src/main/res/layout/`
- `app/src/main/res/navigation/mobile_navigation.xml`
- `app/src/main/res/raw/supabase_config.json`

### Backend
- `supabase/migrations/`
- `supabase/edge_functions/`
- `supabase/sql/`

### Documentacao
- `doc/`

## Arquivos importantes

### Configuracao do app
- `app/src/main/res/raw/supabase_config.json`
  - `url`
  - `anon_key`
  - `test_mode`

### Navegacao
- `app/src/main/res/navigation/mobile_navigation.xml`
- `app/src/main/res/menu/activity_main_drawer.xml`
- `app/src/main/java/com/example/rqm/MainActivity.java`

### Fluxo de materiais
- `app/src/main/java/com/example/rqm/ui/formulario_operacao/FormularioOperacaoFragment.java`
- `app/src/main/java/com/example/rqm/ui/materiais/MateriaisFragment.java`
- `app/src/main/java/com/example/rqm/ui/confirmacao/ConfirmacaoFragment.java`
- `app/src/main/java/com/example/rqm/utils/SupabaseUploader.java`

### Sincronizacao
- `app/src/main/java/com/example/rqm/utils/SyncManager.java`
- `app/src/main/java/com/example/rqm/data/SyncRunDao.java`
- `app/src/main/java/com/example/rqm/ui/sync/SyncRunsFragment.java`

## Banco e regras de materiais

### Saldo fisico
- Tabela: `inventory_balance`
- Nao pode ficar negativo.

### Saldo liquido por projeto
- Tabela: `project_material_balance`
- Guarda:
  - `qty_issued`
  - `qty_returned`
  - `qty_net`

### Historico
- Tabela: `stock_movements`

### Regras
- Requisicao baixa estoque fisico e aumenta saldo liquido do projeto.
- Devolucao sobe estoque fisico e aumenta retorno no projeto.
- Devolucao nao pode exceder o saldo liquido do projeto.
- RPC aplica tudo em transacao unica.

## Convencao de auditoria

Toda tabela de cadastro do SaaS e toda tabela alimentada pelo app deve usar:
- `created_by`
- `updated_by`
- `created_at`
- `updated_at`

Referencia
- `doc/Auditoria_Padrao.txt`
- `supabase/migrations/015_add_audit_columns.sql`

## O que ja existe no Supabase

### Migrations
Ver `supabase/migrations/README.txt`.

### Edge Functions
Ver `supabase/edge_functions/doc/README.txt`.

## Pendencia principal restante
- modelagem final de `projects`

O app esta funcional usando projeto como texto. A migracao para tabela oficial de projetos ainda depende da definicao final do fluxo.

## Como configurar para rodar

1. Preencher `app/src/main/res/raw/supabase_config.json`.
2. Aplicar as migrations em ordem.
3. Publicar as Edge Functions usadas pelo app.
4. Fazer build do app.

## Ordem recomendada de deploy no Supabase

1. Migrations `000` ate `015`.
2. Deploy das Edge Functions:
   - `login_matricula`
   - `verify_admin_pin`
   - `logout`
   - `log_error`
   - `sync_run`
   - `submit_material_request`
   - `get_inventory_balance`
   - `get_project_material_balance`
   - `get_materials`
   - `get_responsaveis`
3. Configurar secrets:
   - `SUPABASE_URL`
   - `SUPABASE_SERVICE_ROLE_KEY`

## Documentacao complementar

- `doc/00_Indice_Documentacao.txt`
- `doc/Fluxo_Aplicativo.txt`
- `doc/Mapa_Arquivos_App.txt`
- `doc/Pendencias_Backend_Supabase.txt`
- `doc/Auditoria_Padrao.txt`
- `doc/Checklist_PreCadastros.txt`
- `doc/Modelo_Materiais.txt`
