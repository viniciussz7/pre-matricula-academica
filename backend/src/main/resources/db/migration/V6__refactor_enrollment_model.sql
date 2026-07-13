-- ======================================================
-- 1. AJUSTAR ENROLLMENT_PROCESS_CLASSES
-- ======================================================

-- A entity EnrollmentProcessClass utiliza um UUID próprio.
ALTER TABLE enrollment_process_classes
ADD COLUMN id UUID NOT NULL DEFAULT gen_random_uuid();

-- A tabela antiga utiliza chave primária composta.
ALTER TABLE enrollment_process_classes
DROP CONSTRAINT enrollment_process_classes_pkey;

-- class_id passa a acompanhar o nome usado pela entity.
ALTER TABLE enrollment_process_classes
RENAME COLUMN class_id TO class_group_id;

-- Campos de auditoria e exclusão lógica utilizados pela entity.
ALTER TABLE enrollment_process_classes
ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE enrollment_process_classes
ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE enrollment_process_classes
ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

-- Nova chave primária.
ALTER TABLE enrollment_process_classes
ADD CONSTRAINT pk_enrollment_process_classes
    PRIMARY KEY (id);

-- Um mesmo processo não pode conter a mesma turma duas vezes.
ALTER TABLE enrollment_process_classes
ADD CONSTRAINT uk_enrollment_process_class
    UNIQUE (
        enrollment_process_id,
        class_group_id
    );


-- ======================================================
-- 2. REFATORAR ENROLLMENTS
-- ======================================================

-- A antiga tabela enrollments representava uma turma escolhida.
-- Agora ela representará a pré-matrícula completa do aluno.

ALTER TABLE enrollments
DROP CONSTRAINT fk_enrollment_class;

ALTER TABLE enrollments
DROP CONSTRAINT uk_student_process_class;

ALTER TABLE enrollments
DROP COLUMN class_id;

-- Um aluno possui somente uma pré-matrícula por processo.
ALTER TABLE enrollments
ADD CONSTRAINT uk_student_enrollment_process
    UNIQUE (
        student_id,
        enrollment_process_id
    );


-- ======================================================
-- 3. CRIAR ENROLLMENT_ITEMS
-- ======================================================

CREATE TABLE enrollment_items (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    enrollment_id UUID NOT NULL,

    enrollment_process_class_id UUID NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_enrollment_item_enrollment
        FOREIGN KEY (enrollment_id)
        REFERENCES enrollments(id),

    CONSTRAINT fk_enrollment_item_process_class
        FOREIGN KEY (enrollment_process_class_id)
        REFERENCES enrollment_process_classes(id),

    CONSTRAINT uk_enrollment_item_process_class
        UNIQUE (
            enrollment_id,
            enrollment_process_class_id
        )
);


-- ======================================================
-- 4. ÍNDICES
-- ======================================================

CREATE INDEX idx_enrollments_process
    ON enrollments(enrollment_process_id);

CREATE INDEX idx_enrollment_items_process_class
    ON enrollment_items(enrollment_process_class_id);