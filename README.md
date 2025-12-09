# Stylo - Agendamento e Gestão de Beleza e Estética

![Language](https://img.shields.io/badge/language-Kotlin-purple)
![Platform](https://img.shields.io/badge/platform-Android-green)
![Architecture](https://img.shields.io/badge/architecture-MVVM-blue)
![Backend](https://img.shields.io/badge/backend-Firebase-orange)

**Stylo** é uma aplicação Android nativa desenvolvida para conectar clientes a estabelecimentos de beleza (barbearias, salões, clínicas de estética) e fornecer uma plataforma robusta de gestão para os proprietários desses negócios.

O projeto foi desenvolvido como parte de um trabalho acadêmico, demonstrando o uso de boas práticas de desenvolvimento Android moderno, arquitetura MVVM e integração completa com serviços em nuvem.

---

## 📱 Visão Geral do Projeto

O **Stylo** atua em duas frentes principais:

1.  **Para o Cliente:** Facilita a busca por profissionais, visualização de portfólio, avaliações e agendamento de serviços de forma rápida e intuitiva.
2.  **Para o Gestor (Estabelecimento):** Oferece um painel administrativo para gerenciar a agenda, faturamento, equipe de funcionários e catálogo de serviços.

---

## 🚀 Funcionalidades Principais

O aplicativo adapta sua interface e funcionalidades dinamicamente com base no perfil do usuário logado (`CLIENTE`, `GESTOR` ou `FUNCIONÁRIO`).

### 👤 Perfil Cliente
* **Busca Inteligente:** Pesquisa de estabelecimentos por nome, cidade ou categoria.
* **Filtros Avançados:** Filtragem por avaliação mínima, cidades disponíveis e categorias específicas.
* **Agendamento:** Escolha de serviços, profissionais e horários disponíveis em tempo real.
* **Favoritos:** Opção de salvar os estabelecimentos preferidos para acesso rápido.
* **Histórico:** Visualização de agendamentos passados e futuros.

### 💼 Perfil Gestor (Dono do Negócio)
* **Dashboard Financeiro:** Visão geral do faturamento do dia e contagem de atendimentos.
* **Gestão de Serviços:** Cadastro, edição e remoção de serviços (preço, duração).
* **Gestão de Equipe:** Cadastro de funcionários e vinculação deles a serviços específicos.
* **Configuração do Estabelecimento:** Definição de horários de funcionamento, endereço, links sociais e métodos de pagamento.
* **Agenda Mestra:** Visualização completa da agenda de todos os funcionários.

### ✂️ Perfil Funcionário
* **Agenda Pessoal:** Visualização focada nos seus próprios agendamentos.
* **Status de Atendimento:** Controle de status do agendamento (Confirmado, Concluído, Cancelado).

---

## 🛠 Tech Stack e Bibliotecas

O projeto foi construído utilizando as tecnologias mais recentes do ecossistema Android:

* **Linguagem:** [Kotlin](https://kotlinlang.org/)
* **Arquitetura:** MVVM (Model-View-ViewModel)
* **Interface (UI):** XML com ViewBinding
* **Injeção de Dependências & Navegação:**
    * Navigation Component (Single Activity Architecture)
    * Fragment KTX (`by viewModels`)
* **Backend (BaaS - Firebase):**
    * **Authentication:** Login por e-mail/senha e gestão de usuários.
    * **Firestore:** Banco de dados NoSQL para armazenar usuários, agendamentos, serviços e estabelecimentos.
    * **Storage:** Armazenamento de imagens de perfil e banners.
* **Outras Bibliotecas:**
    * **Coroutines:** Programação assíncrona.
    * **Coil:** Carregamento e cache de imagens.
    * **Material Design:** Componentes visuais padronizados (BottomSheets, Cards, Chips).

---

## 📂 Estrutura do Projeto

O código está organizado seguindo a separação de responsabilidades da arquitetura MVVM:

Stylo-Android/
├── app/
│   ├── build.gradle.kts
│   ├── google-services.json
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/styloandroid/
│       │   ├── MainActivity.kt
│       │   │
│       │   ├── data/
│       │   │   ├── auth/
│       │   │   ├── model/
│       │   │   │   ├── AppUser.kt
│       │   │   │   ├── Appointment.kt
│       │   │   │   ├── Service.kt
│       │   │   │   └── Review.kt
│       │   │   └── repository/
│       │   │       ├── AuthRepository.kt
│       │   │       ├── BookingRepository.kt
│       │   │       └── EstablishmentRepository.kt
│       │   │
│       │   └── ui/
│       │       ├── auth/
│       │       ├── client/
│       │       ├── manager/
│       │       └── splash/
│       │
│       └── res/
│           ├── layout/
│           ├── navigation/
│           └── values/
│
└── gradle/libs.versions.toml

---

## 🔧 Configuração e Instalação

Para rodar o projeto localmente, siga os passos abaixo:

### Pré-requisitos
* Android Studio Ladybug ou superior.
* JDK 11 (configurado no `build.gradle.kts`).

### Passos
1.  **Clone o repositório:**
    ```bash
    git clone [[https://github.com/seu-usuario/stylo-android.git](https://github.com/savioissa21/Stylo-Android)]
    ```
2.  **Configuração do Firebase:**
    * Crie um projeto no console do Firebase.
    * Habilite o **Authentication** (Email/Password).
    * Habilite o **Firestore Database** e o **Storage**.
    * Baixe o arquivo `google-services.json` e coloque-o na pasta `app/` do projeto.
3.  **Build:**
    * Abra o projeto no Android Studio.
    * Aguarde a sincronização do Gradle.
    * Execute o app em um emulador ou dispositivo físico.

---

## 👨‍💻 Autor

Desenvolvido por Arthur Estrela e Savio Issa

Projeto acadêmico apresentado para a disciplina de **[Programação para Dispositivos Móveis]** no **[IF Goinao Campus Urutaí]**.

---
