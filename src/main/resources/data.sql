INSERT INTO posts ( title, text, likes_count)
VALUES ('Spring Boot Guide', 'Text about Spring Boot...', 5),
       ( 'Java Core', 'Core Java concepts explained...', 2),
       ( 'PostgreSQL Tips', 'Working with SQL and indexes...', 7),
       ( 'REST API Design', 'How to design REST APIs...', 1);

INSERT INTO comments ( text, post_id)
VALUES ( 'Great article!', 1),
       ( 'Very useful', 1),
       ( 'Thanks!', 2),
       ( 'Helped me a lot', 3),
       ( 'Nice explanation', 3),
       ( 'Good read', 4);

INSERT INTO tags ( name)
VALUES ( 'java'),
       ( 'spring'),
       ( 'springboot'),
       ( 'postgresql'),
       ( 'sql'),
       ( 'rest'),
       ( 'backend');

INSERT INTO post_tags (post_id, tag_id)
VALUES (1, 1),
       (1, 2),
       (1, 3);


INSERT INTO post_tags (post_id, tag_id)
VALUES (2, 1);


INSERT INTO post_tags (post_id, tag_id)
VALUES (3, 4),
       (3, 5);


INSERT INTO post_tags (post_id, tag_id)
VALUES (4, 6),
       (4, 7);