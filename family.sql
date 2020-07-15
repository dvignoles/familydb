DROP TABLE IF EXISTS sister CASCADE;
DROP TABLE IF EXISTS brother CASCADE;
DROP TABLE IF EXISTS brother_sister CASCADE;
DROP TABLE IF EXISTS husband_wife CASCADE;
DROP TABLE IF EXISTS family CASCADE;
DROP TABLE IF EXISTS person CASCADE;


------------------------------------------ Definitions --------------------------------------------------------------
-- CREATE TYPE sex AS ENUM ('M','F');

-- Confirm if a person is a certain gender
CREATE OR REPLACE FUNCTION is_sex(person_id INT, is_this_sex sex) RETURNS BOOLEAN AS
$$
DECLARE
    person_sex sex := (SELECT gender
                       FROM person
                       where id = person_id);
BEGIN
    IF person_sex = is_this_sex THEN
        RETURN TRUE;
    ELSE
        RETURN FALSE;
    end if;
END;
$$
    language plpgsql;

CREATE TABLE person
(
    id            SERIAL PRIMARY KEY, -- Unique constraint
    name          TEXT NOT NULL,          -- Not Null constraint
    date_of_birth date NOT NULL,          -- Not Null constraint
    gender        sex  NOT NULL,          -- Not Null constraint
    CONSTRAINT valid_dob CHECK (date_of_birth >= '1900-01-01' AND date_of_birth <= now())
);

CREATE TABLE family
(
    person INT PRIMARY KEY REFERENCES person (id),
    father INT REFERENCES person (id),
    mother INT REFERENCES person (id),
    UNIQUE (person), -- Unique Constraint
    CONSTRAINT father_is_male check (is_sex(father, 'M')),
    CONSTRAINT mother_is_female check (is_sex(mother, 'F'))
);

-- Children of a given couple
SELECT name as child_name, gender as child_gender
FROM (SELECT *
      FROM family fam
               JOIN person on person.id = fam.person
      WHERE father = 3
        AND mother = 2) AS foo;

-- Brother (c brother of d)
CREATE TABLE brother(
    person INT REFERENCES person (id),
    brother_of INT REFERENCES person(id)
    CONSTRAINT brother_is_male check (is_sex(person, 'M')),
    CONSTRAINT self_brother CHECK(person != brother_of)
);

-- Sister
CREATE TABLE sister(
    person INT REFERENCES person (id),
    sister_of INT REFERENCES person(id)
    CONSTRAINT sister_is_female check (is_sex(person, 'F')),
    CONSTRAINT self_sister check(person != sister_of)
);

-- Brother-Sister
CREATE TABLE brother_sister(
    brother INT REFERENCES person(id),
    sister INT REFERENCES person(id),
    CONSTRAINT brother_is_male check (is_sex(brother, 'M')),
    CONSTRAINT sister_is_female check (is_sex(sister, 'F'))
);

-- Husband-Wife
CREATE TABLE husband_wife(
    husband INT REFERENCES person(id),
    wife INT REFERENCES person(id),
    CONSTRAINT husband_is_male check (is_sex(husband, 'M')),
    CONSTRAINT wife_is_female check (is_sex(wife, 'F'))
);

CREATE OR REPLACE FUNCTION get_spouse(person_id INT) RETURNS INT AS
$$
DECLARE
    person_sex sex := (SELECT gender
                       FROM person
                       where id = person_id);
    rhusband int := null;
    rwife int := null;
BEGIN
    IF person_sex = 'F' THEN
        rhusband := (SELECT hw.husband FROM husband_wife hw WHERE wife = person_id);
        RETURN rhusband;
    ELSE
        rwife := (SELECT hw.wife FROM husband_wife hw WHERE husband = person_id);
        RETURN rwife;
    end if;
END;
$$
    language plpgsql;

------------------------------------------ Query Testing --------------------------------------------------------------

-- Child of a given person

SELECT array[father,mother] FROM family where person=1;

-- input : person id
-- output : children ids

-- pseudo:
    -- find family entries where father or mother = person id

SELECT * FROM person where id in (SELECT person FROM family where father = 6 or mother = 6);

-- Grandparents of person

-- input : person id
-- out : persons

-- psuedo:
    -- find parents
    -- find parents parents

-- partial 1
SELECT array[father,mother] FROM family where person = 1;

-- partial 2
SELECT unnest(ARRAY[father,mother]::INT[]) FROM family where person = 1;

-- partial 3
SELECT father,mother FROM family where person in (SELECT unnest(ARRAY[father,mother]::INT[]) FROM family where person = 3);

-- final
SELECT * FROM person where id in
                           (SELECT unnest(ARRAY[father,mother]) FROM family where person in
                                                                                  (SELECT unnest(ARRAY[father,mother]::INT[]) FROM family where person = 1));

-- Sister in law
-- i) wife of a person's brother -- navida is sister-in-law of michael/charlotte
-- ii) sister of a person's wife or husband -- charlotte is navida's sister-in-law, rafi is my "sister-in-law"
-- iii) wife of the brother of a person's wife or husband -- michael's wife is navida's sister in law, cara is my sister in law

-- strategy : union separate query instances

-- i)

-- partial 1
SELECT * FROM person where id in (SELECT person from brother where brother_of = 2);

-- partial 2
SELECT wife from husband_wife where husband in (SELECT id FROM person where id in (SELECT person from brother where brother_of = 2));

-- final
SELECT * FROM person where id in (SELECT wife from husband_wife where husband in (SELECT id FROM person where id in (SELECT person from brother where brother_of = 2)));
SELECT * FROM person where id in (SELECT wife from husband_wife where husband in (SELECT id FROM person where id in (SELECT person from brother where brother_of = 3)));


-- ii)
-- final
SELECT * FROM person where id in (SELECT person FROM sister where sister_of = get_spouse(1));
SELECT * FROM person where id in (SELECT person FROM sister where sister_of = get_spouse(4));

-- iii)

-- brother of persons wife or husband
SELECT * FROM person where id in (SELECT person FROM brother where brother_of = get_spouse(4));
SELECT * FROM person where id in (SELECT person FROM brother where brother_of = get_spouse(1));

-- wife of brother of persons wife or husband
SELECT * FROM person where id in
                           (SELECT wife FROM husband_wife where husband in
                                                                (SELECT id FROM person where id in
                                                                                             (SELECT person FROM brother where brother_of = get_spouse(1))));
SELECT * FROM person where id in
                           (SELECT wife FROM husband_wife where husband in
                                                                (SELECT id FROM person where id in
                                                                                             (SELECT person FROM brother where brother_of = get_spouse(4))));

-- i) + ii) + iii)

-- final ex 1
WITH all_sis_law as (
    SELECT * FROM person where id in (SELECT wife from husband_wife where husband in (SELECT id FROM person where id in (SELECT person from brother where brother_of = 1)))
UNION ALL
SELECT * FROM person where id in (SELECT person FROM sister where sister_of = get_spouse(1))
UNION ALL
SELECT * FROM person where id in
                           (SELECT wife FROM husband_wife where husband in
                                                                (SELECT id FROM person where id in
                                                                                             (SELECT person FROM brother where brother_of = get_spouse(1)))))
SELECT DISTINCT * FROM all_sis_law;

-- final ex 2
WITH all_sis_law as (
    SELECT * FROM person where id in (SELECT wife from husband_wife where husband in (SELECT id FROM person where id in (SELECT person from brother where brother_of = 3)))
UNION ALL
SELECT * FROM person where id in (SELECT person FROM sister where sister_of = get_spouse(3))
UNION ALL
SELECT * FROM person where id in
                           (SELECT wife FROM husband_wife where husband in
                                                                (SELECT id FROM person where id in
                                                                                             (SELECT person FROM brother where brother_of = get_spouse(3)))))
SELECT DISTINCT * FROM all_sis_law;
