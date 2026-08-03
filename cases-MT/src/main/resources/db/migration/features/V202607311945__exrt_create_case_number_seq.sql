-- owner: exrt
-- author: shaik.umar
-- feature: ExRT Request
-- purpose: Persistent case_number sequence (ExR000001…); does not alter existing rows.

CREATE SEQUENCE IF NOT EXISTS exrt_case_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
