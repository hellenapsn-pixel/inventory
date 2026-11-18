# Inventory — versão Node.js

Reimplementação independente em Next.js, React, Node.js e PostgreSQL (Neon), sem Java, Servlets, Tomcat, Maven ou SQLite. Esta é a base da migração; os fluxos de autenticação, biblioteca, listas, avaliações, favoritos, perfis, busca social e OAuth Google precisam ser portados e validados antes de substituir a aplicação original.

## Configuração
Copie `.env.example` para `.env.local`, configure `DATABASE_URL` e `AUTH_SECRET`, depois execute `npm install` e `npm run dev`.

As capas e a marca são reutilizadas do projeto original em `public/assets`. Não coloque credenciais reais no Git.
