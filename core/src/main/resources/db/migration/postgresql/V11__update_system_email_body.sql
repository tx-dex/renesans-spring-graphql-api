
UPDATE multilingual_phrase set message = 'Password reset for Engager'
WHERE id = 517;

UPDATE multilingual_phrase set message = '<h2>Greetings from Engager!</h2><p>Someone has requested a link to change your password. You can do this through this link:</p><p><a href="{{ reset_link }}">Reset my password</a></p><p>The link will be valid for {{ reset_link_expiration_time }}. If you didn''t request this, please ignore this email.</p><p>Best,<br/>Team Engager<br/>© Renesans Engager 2021</p>'
WHERE id = 518;

UPDATE multilingual_phrase set message = 'Account activation for Engager'
WHERE id = 533;

UPDATE multilingual_phrase set message = '<h2>Greetings from Engager!</h2><p>An Engager Key User account has been created for you with username: {{ username }}. To start using the account you need to set up a password. You can do that here:</p><p><a href="{{ reset_link }}">Activate account</a></p><p>The link will be valid for {{ reset_link_expiration_time }}.</p><p>Best,<br/>Team Engager<br/>© Renesans Engager 2021</p>'
WHERE id = 534;