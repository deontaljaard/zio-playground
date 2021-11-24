curl -i \
  -X POST \
  -H "Content-Type: application/json" \
  -d @createCountryPayload.json \
  0.0.0.0:8080/countries
