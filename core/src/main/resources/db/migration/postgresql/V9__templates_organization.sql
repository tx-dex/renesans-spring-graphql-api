INSERT INTO data.customer (id, name, description, created_by, archived, is_default, segment_id, ctm, mtm)
VALUES ('b5d258fc-318c-4238-93da-22b1265b63dc', 'Templates', 'Old segments from previous version of the app', 1, false, false, null, '2021-02-16 12:43:15.926000', '2021-02-16 12:43:15.926000') ON CONFLICT DO NOTHING;