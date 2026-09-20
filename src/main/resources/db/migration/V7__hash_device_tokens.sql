UPDATE device
SET device_token = LOWER(SHA2(device_token, 256))
WHERE device_token IS NOT NULL
  AND device_token <> ''
  AND LENGTH(device_token) <> 64;
