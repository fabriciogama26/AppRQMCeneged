
# 📦 RQM - Requisição de Materiais

Aplicativo Android para gerenciamento de requisições e devoluções de materiais, com suporte a confirmação, histórico e exportação de dados.

---

## ✨ Funcionalidades Concluídas

### 🧾 Formulário de Operação (`FormularioOperacaoFragment`)
- Entrada de dados obrigatórios:
  - Requisitor
  - Usuário
  - Projeto (prefixo + número)
  - Data (com `DatePicker`)
- Validação de formato do projeto (OMI/OII, OS, OM).
- Customização do `hint` do requisitor (requisição ou devolução).
- Navegação para `MateriaisFragment` via `Bundle`.

### 📦 Tela de Materiais (`MateriaisFragment`)
- Leitura de materiais via JSON (`materiais_completos.json`).
- Filtro dinâmico com `AutoCompleteTextView`.
- Lista de materiais via `RecyclerView` com campo de quantidade.
- Validação:
  - Ao menos 1 item válido.
  - Nenhum item com quantidade zerada/vazia.
- Envio de todos os dados via `Bundle` para `ConfirmacaoFragment`.

### ✅ Tela de Confirmação (`ConfirmacaoFragment`)
- Recebe os dados de `FormularioOperacaoFragment` e `MateriaisFragment`.
- Exibe:
  - Dados do formulário.
  - Lista de materiais.
- Botão **Confirmar**:
  - Exibe `Toast` de sucesso.
  - Navega para a tela inicial (`HomeFragment`).

---

## 🔧 Em Desenvolvimento

### ⚙️ Tela de Configuração (`ConfiguracaoFragment`)
- [ ] **Salvar e-mails de destino** (para envio futuro).
- [ ] **Importar materiais** a partir de JSON.
- [ ] **Importar dados de funcionários** via JSON (uso futuro no formulário).
- [ ] **Exportar banco de dados** (SQLite) para compartilhamento.

### 📜 Histórico (`HistoricoFragment`)
- [ ] Listagem das requisições feitas.
- [ ] Exportação para Excel ou JSON.
- [ ] Filtro por data ou tipo (requisicao/devolucao).
- [ ] Botão “compartilhar” **Exportar banco de dados** (JSON)

---

## 📁 Estrutura de Diretórios

```
com.example.rqm/
├── adapters/
│   ├── MaterialAdapter.java
│   └── MaterialResumoAdapter.java
│
├── models/
│   └── Material.java
│
├── ui/
│   ├── configuracao/
│   │   └── ConfiguracaoFragment.java
│   ├── confirmacao/
│   │   └── ConfirmacaoFragment.java
│   ├── formulario_operacao/
│   │   ├── FormularioOperacaoFragment.java
│   │   └── FormularioOperacaoViewModel.java
│   ├── historico/
│   │   └── HistoricoFragment.java *(em breve)*
│   ├── Home/
│   │   ├── HomeFragment.java
│   │   └── HomeViewModel.java
│   ├── materiais/
│   │   ├── MateriaisFragment.java
│   │   └── MateriaisViewModel.java
│   └── sobre/
│       └── SobreFragment.java
│
├── MainActivity.java
```

## 📂 Recursos

```
res/
├── layout/
│   ├── fragment_confirmacao.xml
│   ├── fragment_formulario_operacao.xml
│   ├── fragment_materiais.xml
│   ├── item_material.xml
│   └── item_material_resumo.xml
│   ...
├── navigation/
│   └── mobile_navigation.xml
├── menu/
│   └── activity_main_drawer.xml
├── drawable/
│   └── image_removebg_preview.png (logo)
├── anim/
│   └── logo_animation.xml
├── assets/
│   └── materiais_completos.json
```

---

## 🚧 Próximos Passos

- [ ] Finalizar `ConfiguracaoFragment` com importações e exportações.
- [ ] Armazenar dados em SQLite com Room.
- [ ] Implementar `HistoricoFragment` com filtro e exportação.
- [ ] Adicionar autenticação de usuário (futuro).
- [ ] Envio real de e-mail com dados da requisição (SMTP/Intent).

---

## 📌 Requisitos Técnicos

- Android Studio Arctic Fox ou superior
- SDK mínimo: 21+
- Linguagem: Java
- Padrão de navegação: Jetpack Navigation
- Gerenciamento de estado: `ViewModel` + `LiveData`

---

## 🧑‍💻 Desenvolvedor

Fabricio Gama  
Rio de Janeiro, 2025
