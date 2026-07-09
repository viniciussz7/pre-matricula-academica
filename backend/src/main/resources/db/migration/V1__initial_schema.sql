-- ==========================================================
-- Sistema de Pré-Matrícula Acadêmica
-- Migration: V1__initial_schema.sql
-- Descrição: Criação do esquema inicial do banco de dados.
-- ==========================================================

-- Extensão para geração de UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto";


-- ======================================================
-- USERS
-- ======================================================

CREATE TABLE users (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    full_name VARCHAR(150) NOT NULL,

    email VARCHAR(150) NOT NULL UNIQUE,

    password VARCHAR(255) NOT NULL,

    role VARCHAR(20) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_user_role
        CHECK (role IN ('ADMIN', 'STUDENT'))

);


CREATE TABLE admins (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL UNIQUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_admin_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)

);


CREATE TABLE students (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL UNIQUE,

    registration_number VARCHAR(20) NOT NULL UNIQUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_student_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)

);


-- ======================================================
-- ACADEMIC DOMAIN
-- ======================================================

CREATE TABLE academic_periods (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(20) NOT NULL UNIQUE,

    name VARCHAR(100) NOT NULL,

    start_date DATE NOT NULL,

    end_date DATE NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_period_dates
        CHECK (start_date <= end_date)

);


CREATE TABLE disciplines (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(20) NOT NULL UNIQUE,

    name VARCHAR(120) NOT NULL,

    workload INTEGER NOT NULL,

    prerequisites TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_discipline_workload
        CHECK (workload > 0)

);


CREATE TABLE classes (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(20) NOT NULL,

    name VARCHAR(150) NOT NULL,

    discipline_id UUID NOT NULL,

    academic_period_id UUID NOT NULL,

    vacancies INTEGER NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_class_discipline
        FOREIGN KEY (discipline_id)
        REFERENCES disciplines(id),

    CONSTRAINT fk_class_academic_period
        FOREIGN KEY (academic_period_id)
        REFERENCES academic_periods(id),

    CONSTRAINT uk_class_code_period
        UNIQUE (code, academic_period_id),

    CONSTRAINT chk_class_vacancies
        CHECK (vacancies > 0)

);


-- ======================================================
-- ENROLLMENT PROCESS
-- ======================================================

CREATE TABLE enrollment_processes (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    title VARCHAR(150) NOT NULL,

    academic_period_id UUID NOT NULL,

    start_date DATE NOT NULL,

    end_date DATE NOT NULL,

    allow_over_capacity BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_process_period
        FOREIGN KEY (academic_period_id)
        REFERENCES academic_periods(id),

    CONSTRAINT chk_process_dates
        CHECK (start_date <= end_date)

);


CREATE TABLE enrollment_process_classes (

    enrollment_process_id UUID NOT NULL,

    class_id UUID NOT NULL,

    PRIMARY KEY (
        enrollment_process_id,
        class_id
    ),

    CONSTRAINT fk_epc_process
        FOREIGN KEY (enrollment_process_id)
        REFERENCES enrollment_processes(id),

    CONSTRAINT fk_epc_class
        FOREIGN KEY (class_id)
        REFERENCES classes(id),

    -- Cada turma pode pertencer a apenas um processo de pré-matrícula.
    CONSTRAINT uk_class_single_process
        UNIQUE(class_id)

);


-- ======================================================
-- ENROLLMENTS
-- ======================================================

CREATE TABLE enrollments (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    student_id UUID NOT NULL,

    enrollment_process_id UUID NOT NULL,

    class_id UUID NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_enrollment_student
        FOREIGN KEY (student_id)
        REFERENCES students(id),

    CONSTRAINT fk_enrollment_process
        FOREIGN KEY (enrollment_process_id)
        REFERENCES enrollment_processes(id),

    CONSTRAINT fk_enrollment_class
        FOREIGN KEY (class_id)
        REFERENCES classes(id),

    -- o aluno não pode se inscrever duas vezes na mesma turma dentro do mesmo processo de matrícula
    CONSTRAINT uk_student_process_class
        UNIQUE (
            student_id,
            enrollment_process_id,
            class_id
        )

);