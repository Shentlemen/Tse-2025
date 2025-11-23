-- Script de creacion de base de datos para el sistema de gestion clinica
-- PostgreSQL Database Schema

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 229 (class 1255 OID 16606)
-- Name: update_users_updated_at(); Type: FUNCTION; Schema: public; Owner: postgres
--
--
CREATE FUNCTION update_users_updated_at() RETURNS trigger
    LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$;

SET default_tablespace = '';

SET default_table_access_method = heap;

-- Crear la base de datos (ejecutar como superusuario)
-- CREATE DATABASE clinic_db;
-- \c clinic_db;

-- Tabla de clinicas (multi-tenant)
CREATE TABLE IF NOT EXISTS clinics (
                                              id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(255),
    hcen_endpoint VARCHAR(500),
    logo_path VARCHAR(500),
    theme_colors TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
    );

--
-- TOC entry 5030 (class 0 OID 0)
-- Dependencies: 220
-- Name: TABLE clinics; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON TABLE clinics IS 'Tabla de clinicas para el sistema multi-tenant';


--
-- TOC entry 5031 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN clinics.code; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN clinics.code IS 'Codigo unico identificador de la clinica';


--
-- TOC entry 5032 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN clinics.hcen_endpoint; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN clinics.hcen_endpoint IS 'Endpoint para integracion con HCEN';


--
-- TOC entry 5033 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN clinics.theme_colors; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN clinics.theme_colors IS 'Colores personalizados en formato JSON';


--
-- TOC entry 219 (class 1259 OID 16444)
-- Name: clinics_id_seq; Type: SEQUENCE; Schema: public; Owner: clinic_user
--

CREATE SEQUENCE clinics_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


-- Tabla de documentos clinicos
CREATE TABLE clinical_documents (
                                           id bigint NOT NULL,
                                           created_at timestamp(6) without time zone NOT NULL,
                                           description character varying(1000),
                                           document_type character varying(100) NOT NULL,
                                           file_name character varying(100),
                                           file_path character varying(500),
                                           file_size bigint,
                                           mime_type character varying(100),
                                           rndc_id character varying(100),
                                           title character varying(255) NOT NULL,
                                           updated_at timestamp(6) without time zone,
                                           clinic_id VARCHAR(50) NOT NULL,
                                           patient_id bigint NOT NULL,
                                           professional_id bigint NOT NULL,
                                           specialty_id bigint,
                                           date_of_visit date NOT NULL,
                                           chief_complaint text,
                                           current_illness text,
                                           vital_signs text,
                                           physical_examination text,
                                           diagnosis text,
                                           treatment text,
                                           prescriptions text,
                                           observations text,
                                           next_appointment date,
                                           attachments text,
                                           is_external boolean DEFAULT false,
                                           source_clinic_id character varying(100),
                                           external_clinic_name character varying(255),
                                           external_document_locator text
);
--
-- TOC entry 5021 (class 0 OID 0)
-- Dependencies: 218
-- Name: TABLE clinical_documents; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON TABLE clinical_documents IS 'Documentos clinicos asociados a pacientes, profesionales, clinicas y especialidades';


--
-- TOC entry 5022 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN clinical_documents.vital_signs; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN clinical_documents.vital_signs IS 'JSON con signos vitales: {pressure, temperature, pulse, respiratoryRate, o2Saturation, weight, height, bmi}';


--
-- TOC entry 5023 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN clinical_documents.prescriptions; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN clinical_documents.prescriptions IS 'JSON array con prescripciones: [{medication, dosage, frequency, duration}]';


--
-- TOC entry 5024 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN clinical_documents.attachments; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN clinical_documents.attachments IS 'JSON array con informacion de archivos adjuntos: [{fileName, filePath, fileSize, mimeType}]';


--
-- TOC entry 5025 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN clinical_documents.is_external; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN clinical_documents.is_external IS 'Indica si es un documento descargado de otra clinica';


--
-- TOC entry 5026 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN clinical_documents.source_clinic_id; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN clinical_documents.source_clinic_id IS 'ID de la clinica origen (si es documento externo)';


--
-- TOC entry 5027 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN clinical_documents.external_clinic_name; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN clinical_documents.external_clinic_name IS 'Nombre de la clinica origen (si es documento externo)';


--
-- TOC entry 5028 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN clinical_documents.external_document_locator; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN clinical_documents.external_document_locator IS 'URL original del documento externo';


--
-- TOC entry 217 (class 1259 OID 16435)
-- Name: clinical_documents_id_seq; Type: SEQUENCE; Schema: public; Owner: clinic_user
--

CREATE SEQUENCE clinical_documents_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


-- Tabla de especialidades medicas
CREATE TABLE specialties (
                                    id BIGSERIAL PRIMARY KEY,
                                    name VARCHAR(100) NOT NULL UNIQUE,
                                    description VARCHAR(255),
                                    code VARCHAR(10),
                                    active BOOLEAN NOT NULL DEFAULT TRUE
);


-- Tabla de profesionales
CREATE TABLE IF NOT EXISTS professionals (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    license_number VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    clinic_id VARCHAR(50) NOT NULL,
    specialty_id BIGINT NOT NULL,

    -- Foreign keys
    CONSTRAINT fk_professional_clinic
    FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE CASCADE,
    CONSTRAINT fk_professional_specialty
    FOREIGN KEY (specialty_id) REFERENCES specialties(id) ON DELETE RESTRICT
    );

--
-- TOC entry 228 (class 1259 OID 16479)
-- Name: users; Type: TABLE; Schema: public; Owner: clinic_user
--
CREATE TABLE users (
                              id bigint NOT NULL,
                              active boolean NOT NULL,
                              created_at timestamp(6) without time zone NOT NULL,
                              email character varying(255),
                              password character varying(255) NOT NULL,
                              role character varying(100) NOT NULL,
                              updated_at timestamp(6) without time zone,
                              username character varying(50) NOT NULL,
                              clinic_id VARCHAR(50) NOT NULL,
                              professional_id bigint,
                              first_name character varying(100),
                              last_name character varying(100),
                              last_login timestamp without time zone,
                              created_by bigint
);

CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5044 (class 0 OID 0)
-- Dependencies: 227
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinic_user
--

ALTER SEQUENCE users_id_seq OWNED BY users.id;

CREATE TABLE patients (
                                 id bigint NOT NULL,
                                 active boolean NOT NULL,
                                 address character varying(500),
                                 birth_date date,
                                 created_at timestamp(6) without time zone NOT NULL,
                                 document_number character varying(50),
                                 email character varying(255),
                                 gender character varying(10),
                                 inus_id character varying(50),
                                 last_name character varying(255),
                                 name character varying(255) NOT NULL,
                                 phone character varying(20),
                                 updated_at timestamp(6) without time zone,
                                 clinic_id VARCHAR(50) NOT NULL
);

--
-- TOC entry 221 (class 1259 OID 16453)
-- Name: patients_id_seq; Type: SEQUENCE; Schema: public; Owner: clinic_user
--

CREATE SEQUENCE patients_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- TOC entry 5037 (class 0 OID 0)
-- Dependencies: 221
-- Name: patients_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinic_user
--

ALTER SEQUENCE patients_id_seq OWNED BY patients.id;


--
-- TOC entry 4770 (class 2604 OID 16439)
-- Name: clinical_documents id; Type: DEFAULT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY clinical_documents ALTER COLUMN id SET DEFAULT nextval('clinical_documents_id_seq'::regclass);


--
-- TOC entry 4772 (class 2604 OID 16448)
-- Name: clinics id; Type: DEFAULT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY clinics ALTER COLUMN id SET DEFAULT nextval('clinics_id_seq'::regclass);


--
-- TOC entry 4773 (class 2604 OID 16457)
-- Name: patients id; Type: DEFAULT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY patients ALTER COLUMN id SET DEFAULT nextval('patients_id_seq'::regclass);

--
-- TOC entry 4776 (class 2604 OID 16482)
-- Name: users id; Type: DEFAULT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);


--
-- TOC entry 4788 (class 2606 OID 16443)
-- Name: clinical_documents clinical_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY clinical_documents
    ADD CONSTRAINT clinical_documents_pkey PRIMARY KEY (id);


--
-- TOC entry 4804 (class 2606 OID 16461)
-- Name: patients patients_pkey; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY patients
    ADD CONSTRAINT patients_pkey PRIMARY KEY (id);

--
-- TOC entry 4798 (class 2606 OID 16488)
-- Name: clinics uk_4qahoqilq9w6be87llkt6vpo7; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY clinics
    ADD CONSTRAINT uk_4qahoqilq9w6be87llkt6vpo7 UNIQUE (code);


--
-- TOC entry 4822 (class 2606 OID 16494)
-- Name: specialties uk_bhb8s9o5hv30lkbidtod9cixc; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY specialties
    ADD CONSTRAINT uk_bhb8s9o5hv30lkbidtod9cixc UNIQUE (name);


--
-- TOC entry 4815 (class 2606 OID 16492)
-- Name: professionals uk_e29v2cio38skje6dmcb9mhag2; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY professionals
    ADD CONSTRAINT uk_e29v2cio38skje6dmcb9mhag2 UNIQUE (license_number);


--
-- TOC entry 4817 (class 2606 OID 16490)
-- Name: professionals uk_imggb2hu013m7sme9jkdd0ajn; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY professionals
    ADD CONSTRAINT uk_imggb2hu013m7sme9jkdd0ajn UNIQUE (email);


--
-- TOC entry 4806 (class 2606 OID 16591)
-- Name: patients uk_patients_inus_id; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY patients
    ADD CONSTRAINT uk_patients_inus_id UNIQUE (inus_id);


--
-- TOC entry 4827 (class 2606 OID 16496)
-- Name: users uk_r43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY users
    ADD CONSTRAINT uk_r43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- TOC entry 4829 (class 2606 OID 16486)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 4789 (class 1259 OID 16634)
-- Name: idx_clinical_documents_clinic_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_clinical_documents_clinic_id ON clinical_documents USING btree (clinic_id);


--
-- TOC entry 4790 (class 1259 OID 16638)
-- Name: idx_clinical_documents_date_of_visit; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_clinical_documents_date_of_visit ON clinical_documents USING btree (date_of_visit);


--
-- TOC entry 4791 (class 1259 OID 16639)
-- Name: idx_clinical_documents_document_type; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_clinical_documents_document_type ON clinical_documents USING btree (document_type);


--
-- TOC entry 4792 (class 1259 OID 16636)
-- Name: idx_clinical_documents_patient_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_clinical_documents_patient_id ON clinical_documents USING btree (patient_id);


--
-- TOC entry 4793 (class 1259 OID 16637)
-- Name: idx_clinical_documents_professional_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_clinical_documents_professional_id ON clinical_documents USING btree (professional_id);


--
-- TOC entry 4794 (class 1259 OID 16635)
-- Name: idx_clinical_documents_specialty_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_clinical_documents_specialty_id ON clinical_documents USING btree (specialty_id);


--
-- TOC entry 4799 (class 1259 OID 16589)
-- Name: idx_patients_active; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_patients_active ON patients USING btree (active);


--
-- TOC entry 4800 (class 1259 OID 16586)
-- Name: idx_patients_clinic_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_patients_clinic_id ON patients USING btree (clinic_id);


--
-- TOC entry 4801 (class 1259 OID 16588)
-- Name: idx_patients_document_number; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_patients_document_number ON patients USING btree (document_number);


--
-- TOC entry 4802 (class 1259 OID 16587)
-- Name: idx_patients_inus_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_patients_inus_id ON patients USING btree (inus_id);


--
-- TOC entry 4807 (class 1259 OID 16585)
-- Name: idx_professionals_active; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_professionals_active ON professionals USING btree (active);


--
-- TOC entry 4808 (class 1259 OID 16581)
-- Name: idx_professionals_clinic_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_professionals_clinic_id ON professionals USING btree (clinic_id);


--
-- TOC entry 4809 (class 1259 OID 16583)
-- Name: idx_professionals_email; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_professionals_email ON professionals USING btree (email);


--
-- TOC entry 4810 (class 1259 OID 16584)
-- Name: idx_professionals_license; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_professionals_license ON professionals USING btree (license_number);


--
-- TOC entry 4811 (class 1259 OID 16582)
-- Name: idx_professionals_specialty_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_professionals_specialty_id ON professionals USING btree (specialty_id);


--
-- TOC entry 4823 (class 1259 OID 16592)
-- Name: idx_users_clinic_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_users_clinic_id ON users USING btree (clinic_id);


--
-- TOC entry 4824 (class 1259 OID 16594)
-- Name: idx_users_role; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_users_role ON users USING btree (role);


--
-- TOC entry 4825 (class 1259 OID 16593)
-- Name: idx_users_username; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_users_username ON users USING btree (username);


--
-- TOC entry 4847 (class 2620 OID 16617)
-- Name: users trigger_users_updated_at; Type: TRIGGER; Schema: public; Owner: clinic_user
--

CREATE TRIGGER trigger_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_users_updated_at();


--
-- TOC entry 4836 (class 2606 OID 16522)
-- Name: clinical_documents fk5nnrdtn2p4so3yy1rpgrwqyrl; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY clinical_documents
    ADD CONSTRAINT fk5nnrdtn2p4so3yy1rpgrwqyrl FOREIGN KEY (professional_id) REFERENCES professionals(id);


--
-- TOC entry 4841 (class 2606 OID 16537)
-- Name: professionals fk6m0w8ssexxsjh3pjhdpuavdfj; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY professionals
    ADD CONSTRAINT fk6m0w8ssexxsjh3pjhdpuavdfj FOREIGN KEY (specialty_id) REFERENCES specialties(id);


--
-- TOC entry 4837 (class 2606 OID 16512)
-- Name: clinical_documents fk94a8jmv8iod12ox4ni2axhphq; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY clinical_documents
    ADD CONSTRAINT fk94a8jmv8iod12ox4ni2axhphq FOREIGN KEY (clinic_id) REFERENCES clinics(id);


--
-- TOC entry 4838 (class 2606 OID 16629)
-- Name: clinical_documents fk_clinical_document_specialty; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY clinical_documents
    ADD CONSTRAINT fk_clinical_document_specialty FOREIGN KEY (specialty_id) REFERENCES specialties(id) ON DELETE RESTRICT;


--
-- TOC entry 4844 (class 2606 OID 16542)
-- Name: users fkdtr2ppiyhryd53ovem6c4ehp1; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY users
    ADD CONSTRAINT fkdtr2ppiyhryd53ovem6c4ehp1 FOREIGN KEY (clinic_id) REFERENCES clinics(id);

--
-- TOC entry 4845 (class 2606 OID 16547)
-- Name: users fketbu1x833y73l1au6j2ssw15x; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY users
    ADD CONSTRAINT fketbu1x833y73l1au6j2ssw15x FOREIGN KEY (professional_id) REFERENCES professionals(id);



--
-- TOC entry 4842 (class 2606 OID 16532)
-- Name: professionals fkfwwx4vavghage4efxddbm33u8; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY professionals
    ADD CONSTRAINT fkfwwx4vavghage4efxddbm33u8 FOREIGN KEY (clinic_id) REFERENCES clinics(id);


--
-- TOC entry 4846 (class 2606 OID 16618)
-- Name: users fkibk1e3kaxy5sfyeekp8hbhnim; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY users
    ADD CONSTRAINT fkibk1e3kaxy5sfyeekp8hbhnim FOREIGN KEY (created_by) REFERENCES users(id);


--
-- TOC entry 4839 (class 2606 OID 16517)
-- Name: clinical_documents fkk9quspqkm1rvy8tw92y13qr22; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY clinical_documents
    ADD CONSTRAINT fkk9quspqkm1rvy8tw92y13qr22 FOREIGN KEY (patient_id) REFERENCES patients(id);


--
-- TOC entry 4840 (class 2606 OID 16527)
-- Name: patients fkm78f4ycelhyuh51u08g20dcmd; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY patients
    ADD CONSTRAINT fkm78f4ycelhyuh51u08g20dcmd FOREIGN KEY (clinic_id) REFERENCES clinics(id);


-- Insertar datos iniciales

-- Especialidades medicas basicas
INSERT INTO specialties
(id, active, code, description, name)
VALUES
        (1, TRUE, 'MG', 'Atencion medica general y preventiva', 'Medicina General'),
        (2, TRUE, 'CAR', 'Especialidad en enfermedades del corazon', 'Cardiologia'),
        (3, TRUE, 'DER', 'Especialidad en enfermedades de la piel', 'Dermatologia'),
        (4, TRUE, 'GIN', 'Especialidad en salud femenina', 'Ginecologia'),
        (5, TRUE, 'PED', 'Especialidad en medicina infantil', 'Pediatria'),
        (6, TRUE, 'OFT', 'Especialidad en salud ocular', 'Oftalmologia'),
        (7, TRUE, 'ORL', 'Especialidad en oido, nariz y garganta', 'Otorrinolaringologia'),
        (8, TRUE, 'PSI', 'Especialidad en salud mental', 'Psicologia'),
        (9, TRUE, 'TRA', 'Especialidad en sistema musculoesqueletico', 'Traumatologia'),
        (10, TRUE, 'URO', 'Especialidad en sistema urinario y genital masculino', 'Urologia'),
        (16, TRUE, 'NEURO', 'Especialidad medica del sistema nervioso', 'Neurologia'),
        (30, TRUE, 'ELECTRO', 'Estudios electricos del corazon', 'Electrofisiologia'),
        (31, TRUE, 'NEURO_CIR', 'Cirugia del sistema nervioso', 'Neurocirugia'),
        (32, TRUE, 'NEURO_PSI', 'Evaluacion neuropsicologica', 'Neuropsicologia'),
        (33, TRUE, 'EPILEP', 'Especialidad en epilepsia', 'Epileptologia'),
        (34, TRUE, 'EspPru', 'Especialidad test', 'Especialidad de prueba'),
        (35, TRUE, 'EspPru2', 'Descripcion de especialidad 2', 'Especialidad de prueba 2'),
        (29, TRUE, 'CAR_INTO', 'Procedimientos invasivos del corazon', 'Cardiologia Intervencionista'),
        (37, TRUE, 'MF', 'Atencion integral a la familia', 'Medicina Familiar'),
        (40, TRUE, 'PSQ', 'Especialidad en salud mental y trastornos psiquiatricos', 'Psiquiatria'),
        (41, TRUE, 'PSIC', 'Especialidad en salud mental y terapia psicologica', 'Psicologia Clinica'),
        (43, TRUE, 'GYN', 'Especialidad en salud femenina y embarazo', 'Ginecologia y Obstetricia'),
        (45, TRUE, 'GER', 'Especialidad en salud del adulto mayor', 'Geriatria'),
        (46, TRUE, 'CG', 'Cirugia de enfermedades generales', 'Cirugia General'),
        (47, TRUE, 'CP', 'Cirugia reconstructiva y estetica', 'Cirugia Plastica'),
        (49, TRUE, 'CCV', 'Cirugia del corazon y vasos sanguineos', 'Cirugia Cardiovascular'),
        (53, TRUE, 'RAD', 'Diagnostico por imagenes', 'Radiologia'),
        (54, TRUE, 'AP', 'Diagnostico mediante estudio de tejidos', 'Anatomia Patologica'),
        (55, TRUE, 'MN', 'Diagnostico y tratamiento con isotopos radioactivos', 'Medicina Nuclear'),
        (56, TRUE, 'LAB', 'Analisis clinicos y diagnosticos', 'Laboratorio Clinico'),
        (57, TRUE, 'GAST', 'Especialidad en sistema digestivo', 'Gastroenterologia'),
        (58, TRUE, 'NEUM', 'Especialidad en enfermedades respiratorias', 'Neumologia'),
        (59, TRUE, 'NEF', 'Especialidad en rinones y enfermedades renales', 'Nefrologia'),
        (60, TRUE, 'END', 'Especialidad en glandulas y hormonas', 'Endocrinologia'),
        (61, TRUE, 'REU', 'Especialidad en enfermedades reumaticas', 'Reumatologia'),
        (62, TRUE, 'HEM', 'Especialidad en sangre y organos hematopoyeticos', 'Hematologia'),
        (63, TRUE, 'ONC', 'Especialidad en tratamiento del cancer', 'Oncologia'),
        (64, TRUE, 'TRA', 'Especialidad en sistema musculoesqueletico', 'Traumatologia y Ortopedia'),
        (65, TRUE, 'CPED', 'Cirugia en pacientes pediatricos', 'Cirugia Pediatrica'),
        (66, TRUE, 'CT', 'Cirugia del torax y pulmones', 'Cirugia de Torax'),
        (67, TRUE, 'EMER', 'Atencion medica de urgencias', 'Medicina de Emergencias'),
        (68, TRUE, 'MI', 'Cuidado critico y terapia intensiva', 'Medicina Intensiva'),
        (69, TRUE, 'MT', 'Salud ocupacional y medicina laboral', 'Medicina del Trabajo'),
        (70, TRUE, 'MD', 'Medicina del deporte y actividad fisica', 'Medicina Deportiva'),
        (71, TRUE, 'ALER', 'Especialidad en alergias e inmunologia', 'Alergologia'),
        (72, TRUE, 'INF', 'Especialidad en enfermedades infecciosas', 'Infectologia'),
        (73, TRUE, 'FIS', 'Rehabilitacion y fisiatria', 'Medicina Fisica y Rehabilitacion'),
        (74, TRUE, 'ANE', 'Anestesia y cuidados perioperatorios', 'Anestesiologia');
-- Comentarios sobre la estructura
COMMENT ON TABLE clinics IS 'Tabla de clinicas para el sistema multi-tenant';
COMMENT ON TABLE specialties IS 'Especialidades medicas disponibles';
COMMENT ON TABLE professionals IS 'Profesionales de salud registrados en el sistema';

COMMENT ON COLUMN clinics.code IS 'Codigo unico identificador de la clinica';
COMMENT ON COLUMN clinics.hcen_endpoint IS 'Endpoint para integracion con HCEN';
COMMENT ON COLUMN clinics.theme_colors IS 'Colores personalizados en formato JSON';

COMMENT ON COLUMN professionals.license_number IS 'Numero de matricula profesional';
COMMENT ON COLUMN professionals.active IS 'Indica si el profesional esta activo en el sistema';
