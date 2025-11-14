--
-- PostgreSQL database dump
--

\restrict eXE4ZiF38dCeZMRBOmxojbwjCqOVXrj1cnLMCQ0hV5xoN8ZVqud90ZBom3YPuCC

-- Dumped from database version 16.10
-- Dumped by pg_dump version 16.10

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: update_users_updated_at(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_users_updated_at() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_users_updated_at() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: access_requests; Type: TABLE; Schema: public; Owner: clinic_user
--

CREATE TABLE public.access_requests (
    id bigint NOT NULL,
    expires_at timestamp(6) without time zone,
    reason character varying(1000),
    requested_at timestamp(6) without time zone NOT NULL,
    responded_at timestamp(6) without time zone,
    response_notes character varying(1000),
    status character varying(100) NOT NULL,
    clinic_id bigint NOT NULL,
    patient_id bigint NOT NULL,
    professional_id bigint NOT NULL
);


ALTER TABLE public.access_requests OWNER TO clinic_user;

--
-- Name: access_requests_id_seq; Type: SEQUENCE; Schema: public; Owner: clinic_user
--

CREATE SEQUENCE public.access_requests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.access_requests_id_seq OWNER TO clinic_user;

--
-- Name: access_requests_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinic_user
--

ALTER SEQUENCE public.access_requests_id_seq OWNED BY public.access_requests.id;


--
-- Name: clinical_documents; Type: TABLE; Schema: public; Owner: clinic_user
--

CREATE TABLE public.clinical_documents (
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
    clinic_id bigint NOT NULL,
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
    attachments text
);


ALTER TABLE public.clinical_documents OWNER TO clinic_user;

--
-- Name: TABLE clinical_documents; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON TABLE public.clinical_documents IS 'Documentos clínicos asociados a pacientes, profesionales, clínicas y especialidades';


--
-- Name: COLUMN clinical_documents.vital_signs; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN public.clinical_documents.vital_signs IS 'JSON con signos vitales: {pressure, temperature, pulse, respiratoryRate, o2Saturation, weight, height, bmi}';


--
-- Name: COLUMN clinical_documents.prescriptions; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN public.clinical_documents.prescriptions IS 'JSON array con prescripciones: [{medication, dosage, frequency, duration}]';


--
-- Name: COLUMN clinical_documents.attachments; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN public.clinical_documents.attachments IS 'JSON array con información de archivos adjuntos: [{fileName, filePath, fileSize, mimeType}]';


--
-- Name: clinical_documents_id_seq; Type: SEQUENCE; Schema: public; Owner: clinic_user
--

CREATE SEQUENCE public.clinical_documents_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.clinical_documents_id_seq OWNER TO clinic_user;

--
-- Name: clinical_documents_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinic_user
--

ALTER SEQUENCE public.clinical_documents_id_seq OWNED BY public.clinical_documents.id;


--
-- Name: clinics; Type: TABLE; Schema: public; Owner: clinic_user
--

CREATE TABLE public.clinics (
    id bigint NOT NULL,
    active boolean NOT NULL,
    address character varying(255),
    code character varying(50) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(255),
    email character varying(255),
    hcen_endpoint character varying(255),
    logo_path character varying(255),
    name character varying(255) NOT NULL,
    phone character varying(255),
    theme_colors text,
    updated_at timestamp(6) without time zone
);


ALTER TABLE public.clinics OWNER TO clinic_user;

--
-- Name: TABLE clinics; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON TABLE public.clinics IS 'Tabla de clínicas para el sistema multi-tenant';


--
-- Name: COLUMN clinics.code; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN public.clinics.code IS 'Código único identificador de la clínica';


--
-- Name: COLUMN clinics.hcen_endpoint; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN public.clinics.hcen_endpoint IS 'Endpoint para integración con HCEN';


--
-- Name: COLUMN clinics.theme_colors; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN public.clinics.theme_colors IS 'Colores personalizados en formato JSON';


--
-- Name: clinics_id_seq; Type: SEQUENCE; Schema: public; Owner: clinic_user
--

CREATE SEQUENCE public.clinics_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.clinics_id_seq OWNER TO clinic_user;

--
-- Name: clinics_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinic_user
--

ALTER SEQUENCE public.clinics_id_seq OWNED BY public.clinics.id;


--
-- Name: patients; Type: TABLE; Schema: public; Owner: clinic_user
--

CREATE TABLE public.patients (
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
    clinic_id bigint NOT NULL
);


ALTER TABLE public.patients OWNER TO clinic_user;

--
-- Name: patients_id_seq; Type: SEQUENCE; Schema: public; Owner: clinic_user
--

CREATE SEQUENCE public.patients_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.patients_id_seq OWNER TO clinic_user;

--
-- Name: patients_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinic_user
--

ALTER SEQUENCE public.patients_id_seq OWNED BY public.patients.id;


--
-- Name: professionals; Type: TABLE; Schema: public; Owner: clinic_user
--

CREATE TABLE public.professionals (
    id bigint NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(255),
    last_name character varying(255),
    license_number character varying(100),
    name character varying(255) NOT NULL,
    phone character varying(20),
    updated_at timestamp(6) without time zone,
    clinic_id bigint NOT NULL,
    specialty_id bigint NOT NULL
);


ALTER TABLE public.professionals OWNER TO clinic_user;

--
-- Name: TABLE professionals; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON TABLE public.professionals IS 'Profesionales de salud registrados en el sistema';


--
-- Name: COLUMN professionals.active; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN public.professionals.active IS 'Indica si el profesional está activo en el sistema';


--
-- Name: COLUMN professionals.license_number; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON COLUMN public.professionals.license_number IS 'Número de matrícula profesional';


--
-- Name: professionals_id_seq; Type: SEQUENCE; Schema: public; Owner: clinic_user
--

CREATE SEQUENCE public.professionals_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.professionals_id_seq OWNER TO clinic_user;

--
-- Name: professionals_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinic_user
--

ALTER SEQUENCE public.professionals_id_seq OWNED BY public.professionals.id;


--
-- Name: specialties; Type: TABLE; Schema: public; Owner: clinic_user
--

CREATE TABLE public.specialties (
    id bigint NOT NULL,
    active boolean NOT NULL,
    code character varying(10),
    description character varying(255),
    name character varying(100) NOT NULL,
    clinic_id bigint
);


ALTER TABLE public.specialties OWNER TO clinic_user;

--
-- Name: TABLE specialties; Type: COMMENT; Schema: public; Owner: clinic_user
--

COMMENT ON TABLE public.specialties IS 'Especialidades médicas disponibles';


--
-- Name: specialties_id_seq; Type: SEQUENCE; Schema: public; Owner: clinic_user
--

CREATE SEQUENCE public.specialties_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.specialties_id_seq OWNER TO clinic_user;

--
-- Name: specialties_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinic_user
--

ALTER SEQUENCE public.specialties_id_seq OWNED BY public.specialties.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: clinic_user
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(255),
    password character varying(255) NOT NULL,
    role character varying(100) NOT NULL,
    updated_at timestamp(6) without time zone,
    username character varying(50) NOT NULL,
    clinic_id bigint NOT NULL,
    professional_id bigint,
    first_name character varying(100),
    last_name character varying(100),
    last_login timestamp without time zone,
    created_by bigint
);


ALTER TABLE public.users OWNER TO clinic_user;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: clinic_user
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO clinic_user;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: clinic_user
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: access_requests id; Type: DEFAULT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.access_requests ALTER COLUMN id SET DEFAULT nextval('public.access_requests_id_seq'::regclass);


--
-- Name: clinical_documents id; Type: DEFAULT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.clinical_documents ALTER COLUMN id SET DEFAULT nextval('public.clinical_documents_id_seq'::regclass);


--
-- Name: clinics id; Type: DEFAULT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.clinics ALTER COLUMN id SET DEFAULT nextval('public.clinics_id_seq'::regclass);


--
-- Name: patients id; Type: DEFAULT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.patients ALTER COLUMN id SET DEFAULT nextval('public.patients_id_seq'::regclass);


--
-- Name: professionals id; Type: DEFAULT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.professionals ALTER COLUMN id SET DEFAULT nextval('public.professionals_id_seq'::regclass);


--
-- Name: specialties id; Type: DEFAULT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.specialties ALTER COLUMN id SET DEFAULT nextval('public.specialties_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Data for Name: access_requests; Type: TABLE DATA; Schema: public; Owner: clinic_user
--

COPY public.access_requests (id, expires_at, reason, requested_at, responded_at, response_notes, status, clinic_id, patient_id, professional_id) FROM stdin;
\.


--
-- Data for Name: clinical_documents; Type: TABLE DATA; Schema: public; Owner: clinic_user
--

COPY public.clinical_documents (id, created_at, description, document_type, file_name, file_path, file_size, mime_type, rndc_id, title, updated_at, clinic_id, patient_id, professional_id, specialty_id, date_of_visit, chief_complaint, current_illness, vital_signs, physical_examination, diagnosis, treatment, prescriptions, observations, next_appointment, attachments) FROM stdin;
1	2025-11-05 23:42:29.557844	descripcion 1	DIAGNOSTICO	\N	\N	\N	\N	\N	Documento Test 1	2025-11-06 19:31:27.834929	4	2	28	29	2025-11-07	Consulta documento test 1	Historia de la enfermedad actual test 1	{"pressure":"120/80","temperature":"36","pulse":"78","respiratoryRate":"16","o2Saturation":"98","weight":"83","height":"173","bmi":"27.73"}	Examen fisico test 1	Diagnostico test 1	Indicaciones tratamiento test 1	[{"medication":"Medicamento test1","dosage":"2 pastillas","frequency":"por dia","duration":"6 meses"}]	Observaciones test 1	2025-11-27	[{"fileName":"arquitectura-grupo9-tse.pdf","filePath":"uploads\\\\4\\\\2025\\\\1\\\\50556c32-a43d-4b95-9270-2cebb3171e3e.pdf","fileSize":1725165,"mimeType":"application/pdf"}]
\.


--
-- Data for Name: clinics; Type: TABLE DATA; Schema: public; Owner: clinic_user
--

COPY public.clinics (id, active, address, code, created_at, description, email, hcen_endpoint, logo_path, name, phone, theme_colors, updated_at) FROM stdin;
1	t	Av. 18 de Julio 1234, Montevideo	CSJ	2025-10-20 19:12:17.25968	Clínica privada de atención médica integral	info@clinicasanjose.com.uy	\N	\N	Clínica San José	098123456	\N	\N
4	t	Av. 18 de Julio 1234, Montevideo	CLIN001	2025-10-22 21:58:31.603997	Clínica especializada en cardiología	info@clinicacorazon.com.uy	http://localhost:8080/hcen/api	\N	Clínica del Corazón	+598 2 123-4567	{"primary":"#e74c3c","secondary":"#c0392b"}	\N
5	t	Bvar. Artigas 5678, Montevideo	CLIN002	2025-10-22 21:58:31.603997	Centro especializado en neurología	contacto@centroneurologico.com.uy	http://localhost:8080/hcen/api	\N	Centro Neurológico	+598 2 987-6543	{"primary":"#3498db","secondary":"#2980b9"}	\N
\.


--
-- Data for Name: patients; Type: TABLE DATA; Schema: public; Owner: clinic_user
--

COPY public.patients (id, active, address, birth_date, created_at, document_number, email, gender, inus_id, last_name, name, phone, updated_at, clinic_id) FROM stdin;
8	t	Pocitos 789, Montevideo	1978-12-03	2025-10-27 20:52:13.491467	87654321	roberto.martinez@email.com	M	INUS008	Martínez	Roberto	+598 99 999-0000	\N	5
9	t	Centro 789, Montevideo	1983-05-18	2025-10-27 20:52:13.491467	66778899	diego.sanchez@email.com	M	INUS009	Sánchez	Diego	+598 99 678-9012	\N	5
10	t	Prado 2345, Montevideo	1995-01-25	2025-10-27 20:52:13.491467	77889900	carolina.torres@email.com	F	INUS010	Torres	Carolina	+598 99 789-0123	\N	5
11	t	Sayago 6789, Montevideo	1980-06-12	2025-10-27 20:52:13.491467	88990011	martin.vasquez@email.com	M	INUS011	Vásquez	Martín	+598 99 890-1234	\N	5
12	t	Capurro 3456, Montevideo	1993-04-03	2025-10-27 20:52:13.491467	99001122	patricia.morales@email.com	F	INUS012	Morales	Patricia	+598 99 901-2345	\N	5
13	t	Aguada 7890, Montevideo	1977-10-19	2025-10-27 20:52:13.491467	00112233	fernando.gutierrez@email.com	M	INUS013	Gutiérrez	Fernando	+598 99 012-3456	\N	5
14	t	Reducto 4567, Montevideo	1989-08-27	2025-10-27 20:52:13.491467	11223344	ana.ruiz@email.com	F	INUS014	Ruiz	Ana	+598 99 123-4567	\N	5
15	t	Cerro 5678, Montevideo	1984-03-14	2025-10-27 20:52:13.491467	22334455	ricardo.mendoza@email.com	M	INUS015	Mendoza	Ricardo	+598 99 234-5678	\N	5
1	t	Av. 18 de Julio 2345, Montevideo	1990-03-15	2025-10-27 20:52:13.491467	11223345	maria.gonzalez@email.com	F	INUS001	González	María	+598 99 123-4567	\N	4
2	t	Pocitos 567, Montevideo	1985-07-22	2025-10-27 20:52:13.491467	22334455	carlos.perez@email.com	M	INUS002	Pérez	Carlos	+598 99 234-5678	\N	4
3	t	Carrasco 890, Montevideo	1992-11-08	2025-10-27 20:52:13.491467	33445566	laura.rodriguez@email.com	F	INUS003	Rodríguez	Laura	+598 99 345-6789	\N	4
4	t	Malvín Norte 1234, Montevideo	1975-02-14	2025-10-27 20:52:13.491467	44556677	pedro.lopez@email.com	M	INUS004	López	Pedro	+598 99 456-7890	\N	4
5	t	Cordón 4567, Montevideo	1988-09-30	2025-10-27 20:52:13.491467	55667788	isabel.fernandez@email.com	F	INUS005	Fernández	Isabel	+598 99 567-8901	\N	4
6	t	Parque Rodó 789, Montevideo	1993-04-18	2025-10-27 20:52:13.491467	66778899	sofia.martinez@email.com	F	INUS006	Martínez	Sofía	+598 99 678-9012	\N	4
7	f	Buceo 2345, Montevideo	1982-12-05	2025-10-27 20:52:13.491467	77889900	miguel.garcia@email.com	M	INUS007	García	Miguel	+598 99 789-0123	2025-10-27 21:51:36.804593	4
\.


--
-- Data for Name: professionals; Type: TABLE DATA; Schema: public; Owner: clinic_user
--

COPY public.professionals (id, active, created_at, email, last_name, license_number, name, phone, updated_at, clinic_id, specialty_id) FROM stdin;
27	t	2025-10-23 20:24:38.16073	ana.fernandez.torres@clinicacorazon.com.uy	Fernández Torres	LP50004	Dra. Ana Lucía	+598 99 456 789	\N	4	1
28	t	2025-10-23 20:24:38.16073	carmen.rodriguez.vargas@clinicacorazon.com.uy	Rodríguez Vargas	LP50005	Dra. Carmen	+598 99 567 890	\N	4	5
29	t	2025-10-23 20:24:38.16073	diego.silva.morales@clinicacorazon.com.uy	Silva Morales	LP50006	Lic. Diego	+598 99 678 901	\N	4	8
30	t	2025-10-23 20:24:38.16073	carlos.lopez.herrera@centroneurologico.com.uy	López Herrera	LP60001	Dr. Carlos	+598 99 789 012	\N	5	2
31	t	2025-10-23 20:24:38.16073	patricia.mendez.rojas@centroneurologico.com.uy	Méndez Rojas	LP60002	Dra. Patricia	+598 99 890 123	\N	5	2
32	t	2025-10-23 20:24:38.16073	fernando.garcia.morales@centroneurologico.com.uy	García Morales	LP60003	Dr. Fernando	+598 99 901 234	\N	5	1
33	t	2025-10-23 20:24:38.16073	laura.herrera.vargas@centroneurologico.com.uy	Herrera Vargas	LP60004	Dra. Laura	+598 99 012 345	\N	5	1
34	t	2025-10-23 20:24:38.16073	sofia.vargas.perez@centroneurologico.com.uy	Vargas Pérez	LP60005	Dra. Sofía	+598 99 123 456	\N	5	5
2	t	2025-10-20 19:12:17.25968	maria.gonzalez@clinicasanjose.com.uy	Elena González	LP67890	María	099234567	2025-10-21 20:59:15.331297	1	8
1	t	2025-10-20 19:12:17.25968	juan.perez@clinicasanjose.com.uy	Carlos Perez	LP12345	Juan	098123456	2025-10-21 20:59:30.4947	1	3
3	t	2025-10-21 21:00:10.541706	asdfas@gmail.com	ROdao	asdrfa3213	German	18927363	2025-10-21 21:00:27.452963	1	3
6	t	2025-10-22 22:00:07.460122	jperez@clinicacorazon.com.uy	Pérez	LIC001	Dr. Juan	+598 99 111-2222	\N	4	2
7	t	2025-10-22 22:00:07.460122	crodriguez@clinicacorazon.com.uy	Rodríguez	LIC003	Dr. Carlos	+598 99 555-6666	\N	4	5
8	t	2025-10-22 22:00:07.460122	mgonzalez@centroneurologico.com.uy	González	LIC002	Dra. María	+598 99 333-4444	\N	5	16
24	t	2025-10-23 20:24:38.16073	juan.perez.carvalho@clinicacorazon.com.uy	Pérez Carvalho	LP50001	Dr. Juan Carlos	+598 99 123 456	\N	4	2
25	t	2025-10-23 20:24:38.16073	maria.gonzalez.silva@clinicacorazon.com.uy	González Silva	LP50002	Dra. María Elena	+598 99 234 567	\N	4	2
35	t	2025-10-23 20:24:38.16073	martin.rojas.gonzalez@centroneurologico.com.uy	Rojas González	LP60006	Lic. Martín	+598 99 234 567	\N	5	8
36	t	2025-10-23 20:24:38.16073	alejandro.morales.martinez@centroneurologico.com.uy	Morales Martínez	LP60007	Dr. Alejandro	+598 99 345 678	\N	5	9
26	f	2025-10-23 20:24:38.16073	roberto.martinez.lopez@clinicacorazon.com.uy	Martínez López	LP50003	Dr. Roberto	+598 99 345 678	2025-10-27 21:46:07.226942	4	1
37	t	2025-10-28 19:10:28.103429	gerberto@gmail.com	Nelson	L77	Gerberto	39876423	\N	4	30
38	t	2025-10-28 20:40:19.647484	alberflam@gmail.com	Flamingo	L0923	Alberto	897162346	\N	5	33
\.


--
-- Data for Name: specialties; Type: TABLE DATA; Schema: public; Owner: clinic_user
--

COPY public.specialties (id, active, code, description, name, clinic_id) FROM stdin;
1	t	MG	Atención médica general y preventiva	Medicina General	1
2	t	CAR	Especialidad en enfermedades del corazón	Cardiología	1
3	t	DER	Especialidad en enfermedades de la piel	Dermatología	1
4	t	GIN	Especialidad en salud femenina	Ginecología	1
5	t	PED	Especialidad en medicina infantil	Pediatría	1
6	t	OFT	Especialidad en salud ocular	Oftalmología	1
7	t	ORL	Especialidad en oído, nariz y garganta	Otorrinolaringología	1
8	t	PSI	Especialidad en salud mental	Psicología	1
9	t	TRA	Especialidad en sistema musculoesquelético	Traumatología	1
10	t	URO	Especialidad en sistema urinario y genital masculino	Urología	1
16	t	NEURO	Especialidad médica del sistema nervioso	Neurología	1
30	t	ELECTRO	Estudios eléctricos del corazón	Electrofisiología	4
31	t	NEURO_CIR	Cirugía del sistema nervioso	Neurocirugía	5
32	t	NEURO_PSI	Evaluación neuropsicológica	Neuropsicología	5
33	t	EPILEP	Especialidad en epilepsia	Epileptología	5
34	t	EspPru	Especialidad test	Especialidad de prueba	4
35	t	EspPru2	Descripcion de especialidad 2	Especialidad de prueba 2	5
29	t	CAR_INTO	Procedimientos invasivos del corazón	Cardiología Intervencionista	4
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: clinic_user
--

COPY public.users (id, active, created_at, email, password, role, updated_at, username, clinic_id, professional_id, first_name, last_name, last_login, created_by) FROM stdin;
4	t	2025-10-22 22:04:57.733677	\N	$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi	PROFESSIONAL	2025-10-28 16:32:28.784034	prof2	5	8	\N	\N	\N	\N
3	t	2025-10-22 22:04:57.733677	admin2@gmail.com	$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi	ADMIN_CLINIC	2025-10-28 17:02:18.1036	admin2	5	\N	Nombre2	Apellido2	\N	\N
5	t	2025-10-28 17:13:06.331015	admin3@gmail.com	$2a$10$I2oytg6xBUChFNpupeDEFOReg.p7aG8OEyh2lWa.1AD6rz2hWvU4C	ADMIN_CLINIC	\N	admin3	1	\N	Nombre3	Apellido3	\N	\N
1	t	2025-10-22 22:04:57.733677	admin@gmail.com	$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi	ADMIN_CLINIC	2025-10-28 19:41:13.741393	admin	4	\N	nombreAdmin	apellidoAdmin	\N	\N
2	f	2025-10-22 22:04:57.733677	prdfo@gmail.com	$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi	PROFESSIONAL	2025-10-28 20:19:16.72604	prof	4	6	prfo	asnnel	\N	\N
7	t	2025-10-28 20:40:19.719927	alberflam@gmail.com	$2a$10$CxjY30AfX5tasfwBVwDiwOjPNJRNIHUiAxx.O36lS0EIWXbq13MuC	PROFESSIONAL	2025-10-28 20:51:14.806518	L0923	5	38	Alberto	Flamingo	2025-10-28 20:51:14.806822	\N
6	t	2025-10-28 19:10:28.21501	gerberto@gmail.com	$2a$10$.Bkm1MsIMyBinQHDO.HC0ODAyn2LeK2T5JcHOvknJfoWRzigG9c82	PROFESSIONAL	2025-11-05 23:27:13.450098	L77	4	37	Gerberto	Nelson	2025-11-05 23:27:13.45014	\N
\.


--
-- Name: access_requests_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinic_user
--

SELECT pg_catalog.setval('public.access_requests_id_seq', 1, false);


--
-- Name: clinical_documents_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinic_user
--

SELECT pg_catalog.setval('public.clinical_documents_id_seq', 1, true);


--
-- Name: clinics_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinic_user
--

SELECT pg_catalog.setval('public.clinics_id_seq', 7, true);


--
-- Name: patients_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinic_user
--

SELECT pg_catalog.setval('public.patients_id_seq', 15, true);


--
-- Name: professionals_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinic_user
--

SELECT pg_catalog.setval('public.professionals_id_seq', 38, true);


--
-- Name: specialties_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinic_user
--

SELECT pg_catalog.setval('public.specialties_id_seq', 35, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinic_user
--

SELECT pg_catalog.setval('public.users_id_seq', 7, true);


--
-- Name: access_requests access_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.access_requests
    ADD CONSTRAINT access_requests_pkey PRIMARY KEY (id);


--
-- Name: clinical_documents clinical_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.clinical_documents
    ADD CONSTRAINT clinical_documents_pkey PRIMARY KEY (id);


--
-- Name: clinics clinics_pkey; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.clinics
    ADD CONSTRAINT clinics_pkey PRIMARY KEY (id);


--
-- Name: patients patients_pkey; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.patients
    ADD CONSTRAINT patients_pkey PRIMARY KEY (id);


--
-- Name: professionals professionals_pkey; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.professionals
    ADD CONSTRAINT professionals_pkey PRIMARY KEY (id);


--
-- Name: specialties specialties_pkey; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.specialties
    ADD CONSTRAINT specialties_pkey PRIMARY KEY (id);


--
-- Name: clinics uk_4qahoqilq9w6be87llkt6vpo7; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.clinics
    ADD CONSTRAINT uk_4qahoqilq9w6be87llkt6vpo7 UNIQUE (code);


--
-- Name: specialties uk_bhb8s9o5hv30lkbidtod9cixc; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.specialties
    ADD CONSTRAINT uk_bhb8s9o5hv30lkbidtod9cixc UNIQUE (name);


--
-- Name: professionals uk_e29v2cio38skje6dmcb9mhag2; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.professionals
    ADD CONSTRAINT uk_e29v2cio38skje6dmcb9mhag2 UNIQUE (license_number);


--
-- Name: professionals uk_imggb2hu013m7sme9jkdd0ajn; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.professionals
    ADD CONSTRAINT uk_imggb2hu013m7sme9jkdd0ajn UNIQUE (email);


--
-- Name: patients uk_patients_inus_id; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.patients
    ADD CONSTRAINT uk_patients_inus_id UNIQUE (inus_id);


--
-- Name: users uk_r43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_r43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: idx_clinical_documents_clinic_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_clinical_documents_clinic_id ON public.clinical_documents USING btree (clinic_id);


--
-- Name: idx_clinical_documents_date_of_visit; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_clinical_documents_date_of_visit ON public.clinical_documents USING btree (date_of_visit);


--
-- Name: idx_clinical_documents_document_type; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_clinical_documents_document_type ON public.clinical_documents USING btree (document_type);


--
-- Name: idx_clinical_documents_patient_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_clinical_documents_patient_id ON public.clinical_documents USING btree (patient_id);


--
-- Name: idx_clinical_documents_professional_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_clinical_documents_professional_id ON public.clinical_documents USING btree (professional_id);


--
-- Name: idx_clinical_documents_specialty_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_clinical_documents_specialty_id ON public.clinical_documents USING btree (specialty_id);


--
-- Name: idx_patients_active; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_patients_active ON public.patients USING btree (active);


--
-- Name: idx_patients_clinic_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_patients_clinic_id ON public.patients USING btree (clinic_id);


--
-- Name: idx_patients_document_number; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_patients_document_number ON public.patients USING btree (document_number);


--
-- Name: idx_patients_inus_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_patients_inus_id ON public.patients USING btree (inus_id);


--
-- Name: idx_professionals_active; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_professionals_active ON public.professionals USING btree (active);


--
-- Name: idx_professionals_clinic_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_professionals_clinic_id ON public.professionals USING btree (clinic_id);


--
-- Name: idx_professionals_email; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_professionals_email ON public.professionals USING btree (email);


--
-- Name: idx_professionals_license; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_professionals_license ON public.professionals USING btree (license_number);


--
-- Name: idx_professionals_specialty_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_professionals_specialty_id ON public.professionals USING btree (specialty_id);


--
-- Name: idx_specialties_clinic_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_specialties_clinic_id ON public.specialties USING btree (clinic_id);


--
-- Name: idx_users_clinic_id; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_users_clinic_id ON public.users USING btree (clinic_id);


--
-- Name: idx_users_role; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_users_role ON public.users USING btree (role);


--
-- Name: idx_users_username; Type: INDEX; Schema: public; Owner: clinic_user
--

CREATE INDEX idx_users_username ON public.users USING btree (username);


--
-- Name: users trigger_users_updated_at; Type: TRIGGER; Schema: public; Owner: clinic_user
--

CREATE TRIGGER trigger_users_updated_at BEFORE UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.update_users_updated_at();


--
-- Name: clinical_documents fk5nnrdtn2p4so3yy1rpgrwqyrl; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.clinical_documents
    ADD CONSTRAINT fk5nnrdtn2p4so3yy1rpgrwqyrl FOREIGN KEY (professional_id) REFERENCES public.professionals(id);


--
-- Name: professionals fk6m0w8ssexxsjh3pjhdpuavdfj; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.professionals
    ADD CONSTRAINT fk6m0w8ssexxsjh3pjhdpuavdfj FOREIGN KEY (specialty_id) REFERENCES public.specialties(id);


--
-- Name: clinical_documents fk94a8jmv8iod12ox4ni2axhphq; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.clinical_documents
    ADD CONSTRAINT fk94a8jmv8iod12ox4ni2axhphq FOREIGN KEY (clinic_id) REFERENCES public.clinics(id);


--
-- Name: clinical_documents fk_clinical_document_specialty; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.clinical_documents
    ADD CONSTRAINT fk_clinical_document_specialty FOREIGN KEY (specialty_id) REFERENCES public.specialties(id) ON DELETE RESTRICT;


--
-- Name: specialties fk_specialties_clinic; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.specialties
    ADD CONSTRAINT fk_specialties_clinic FOREIGN KEY (clinic_id) REFERENCES public.clinics(id);


--
-- Name: users fkdtr2ppiyhryd53ovem6c4ehp1; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fkdtr2ppiyhryd53ovem6c4ehp1 FOREIGN KEY (clinic_id) REFERENCES public.clinics(id);


--
-- Name: users fketbu1x833y73l1au6j2ssw15x; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fketbu1x833y73l1au6j2ssw15x FOREIGN KEY (professional_id) REFERENCES public.professionals(id);


--
-- Name: access_requests fkfrk0up0mpiga5j25g44w23w02; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.access_requests
    ADD CONSTRAINT fkfrk0up0mpiga5j25g44w23w02 FOREIGN KEY (professional_id) REFERENCES public.professionals(id);


--
-- Name: professionals fkfwwx4vavghage4efxddbm33u8; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.professionals
    ADD CONSTRAINT fkfwwx4vavghage4efxddbm33u8 FOREIGN KEY (clinic_id) REFERENCES public.clinics(id);


--
-- Name: access_requests fki2tjq37bskljyaunnac4lsvnc; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.access_requests
    ADD CONSTRAINT fki2tjq37bskljyaunnac4lsvnc FOREIGN KEY (patient_id) REFERENCES public.patients(id);


--
-- Name: users fkibk1e3kaxy5sfyeekp8hbhnim; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fkibk1e3kaxy5sfyeekp8hbhnim FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: clinical_documents fkk9quspqkm1rvy8tw92y13qr22; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.clinical_documents
    ADD CONSTRAINT fkk9quspqkm1rvy8tw92y13qr22 FOREIGN KEY (patient_id) REFERENCES public.patients(id);


--
-- Name: patients fkm78f4ycelhyuh51u08g20dcmd; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.patients
    ADD CONSTRAINT fkm78f4ycelhyuh51u08g20dcmd FOREIGN KEY (clinic_id) REFERENCES public.clinics(id);


--
-- Name: access_requests fkr5lso0b4s30m9gvt69njkvlqp; Type: FK CONSTRAINT; Schema: public; Owner: clinic_user
--

ALTER TABLE ONLY public.access_requests
    ADD CONSTRAINT fkr5lso0b4s30m9gvt69njkvlqp FOREIGN KEY (clinic_id) REFERENCES public.clinics(id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: pg_database_owner
--

GRANT ALL ON SCHEMA public TO clinic_user;


--
-- PostgreSQL database dump complete
--

\unrestrict eXE4ZiF38dCeZMRBOmxojbwjCqOVXrj1cnLMCQ0hV5xoN8ZVqud90ZBom3YPuCC

