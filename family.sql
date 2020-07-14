CREATE TYPE sex AS ENUM ('M','F');

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
DROP TABLE brother;
CREATE TABLE brother(
    person INT REFERENCES person (id),
    brother_of INT REFERENCES person(id)
    CONSTRAINT brother_is_male check (is_sex(person, 'M')),
    CONSTRAINT self_brother CHECK(person != brother_of)
);


-- Sister
DROP TABLE sister;
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

-- Sister in law
-- i) wife of a person's brother -- navida is sister-in-law of michael/charlotte
-- ii) sister of a person's wife or husband -- charlotte is navida's sister-in-law, rafi is my "sister-in-law"
-- iii) wife of the brother of a person's wife or husband -- michael's wife is navida's sister in law, cara is my sister in law

DROP TABLE sister;
DROP TABLE brother;
DROP TABLE brother_sister;
DROP TABLE husband_wife;
DROP TABLE family;
DROP TABLE person;

SELECT name as child_name, gender as child_gender
FROM (SELECT *
      FROM family fam
               JOIN person on person.id = fam.person
      WHERE father = 5
    ) AS foo;