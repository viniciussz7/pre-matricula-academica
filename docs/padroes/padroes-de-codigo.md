# PADRÕES DO PROJETO
Esse documento vai ajudar a padronizar a codificação.

---

## 1. Linguagem
**Código:** Inglês
**Interface:** Português

Exemplo:
```text
Student, Enrollment, EnrollmentProcess, AcademicPeriod
```
Na tela:
```text
Aluno, Pré-Matrícula, Período Letivo
```

**Padrão de Idioma:**
- Todo o código-fonte será escrito em inglês. 
- Toda a documentação do projeto será escrita em português. 
- A interface do usuário será apresentada em português. 
- Os commits seguirão o padrão Conventional Commits em inglês. 

---

## 2. Organização do Backend

Seguiremos a arquitetura MVC conforme solicitado. Os Controllers estão agrupados na camada de apresentação, os Models representam o domínio da aplicação, os Services concentram as regras de negócio e os Repositories fazem a persistência. Como o projeto será desenvolvido por três integrantes, organizamos cada camada em subpacotes por domínio para facilitar a manutenção, reduzir conflitos e melhorar a navegação no código.

Ou seja:

```backend

src/main/java

br.edu.ifpb.prematricula

│
├── controller
│      ├── auth
│      ├── student
│      ├── discipline
│      ├── process
│      └── enrollment
│
├── service
│      ├── student
│      ├── discipline
│      ├── process
│      └── enrollment
│
├── repository
│      ├── student
│      ├── discipline
│      ├── process
│      └── enrollment
│
├── model
│      ├── entity
│      ├── dto
│      └── enums
│
├── config
├── security
├── exception
└── util
...
```

---

## 3. Organização do Frontend
Será organizado por Feature.
```text
app/
core/
shared/
layout/
features/
auth/
```
Dentro de Features:
```text
student/
discipline/
classgroup/
academicperiod/
enrollmentprocess/
enrollment/
reports/
```
Cada módulo terá:
```text
components/
pages/
services/
models/
```
---

## 4. DTO
Nunca retornaremos Entity.
Sempre:
```text
Controller

↓

DTO

↓

Service

↓

Entity
```

---

## 5. Services
Toda regra de negócio fica aqui.
Nunca no Controller.
Nunca no Repository.

---

## 6. Repository
Somente acesso ao banco.
Nada de lógica.

---

## 7. Controllers
Apenas:
- receber requisição; 
- validar; 
- chamar Service; 
- devolver resposta. 

---

## 8. Exceptions
Nunca:
```java
throw new RuntimeException();
```
Sempre:
```java
StudentNotFoundException

DisciplineNotFoundException

EnrollmentAlreadyExistsException

EnrollmentClosedException
```
---

## 9. Banco
snake_case
```text
academic_period
enrollment_process
pre_enrollment
```

---

## 10.  Java
Classe
```java
StudentService
```
Variável
```java
studentRepository
```
Método
```java
findById()
```
Constante
```java
MAX_ENROLLMENTS
```
---

## 11.  Angular
Componentes
```text
student-list

student-form

discipline-table
```
Nunca:
```text
StudentTelaNovaComponent
```

---

## 12.  Commits
Vamos usar algo próximo do Conventional Commits.
```bash
feat: Nova funcionalidade
fix: Correção
docs: Documentação
refactor: Refatoração
style: Formatação
test: Testes
```
---

## 13.  Branches
Nada de GitFlow.
Somente: **main** e **develop**

A ideia seria:

main: sempre estável e apresentável.
develop: onde vocês três trabalham no dia a dia.

Assim, todos fazem:
```bash
git pull origin develop

git push origin develop
```

Somente quando uma funcionalidade importante estiver funcionando (por exemplo, "CRUD de Alunos completo"), alguém faz o merge de develop para main.

---

## 14.  Pull Request
Mesmo sendo somente três pessoas, seria bom.

Quem desenvolveu: Não faz merge da própria branch.
Outro integrante revisa rapidamente.
Merge.
Leva menos de 3 minutos.
Evita muito problema.

---

## 15.  Organização das tarefas
Vamos tentar usar GitHub Projects.
```text
Backlog

↓

To Do

↓

Doing

↓

Done
```
Cada card representa uma funcionalidade.

---

## 16.  README
O README será construído desde o primeiro dia.

Ele conterá:
- Objetivo do projeto 
- Tecnologias 
- Arquitetura 
- Como executar 
- Estrutura de pastas 
- Integrantes 
- Prints (ao final)
