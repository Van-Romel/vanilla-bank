CREATE TABLE IF NOT EXISTS "vanilla_bank".cliente
(
    clt_id   bigint                 NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1),
    clt_cpf  character varying(11)  NOT NULL,
    clt_nome character varying(255) NOT NULL,
    CONSTRAINT cliente_pkey PRIMARY KEY (clt_id),
    CONSTRAINT uc_cliente_clt_cpf UNIQUE (clt_cpf)
)