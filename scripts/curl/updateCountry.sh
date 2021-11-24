curl -i \
  -X PATCH \
  -H "Content-Type: application/json" \
  -d @updateCountryPayload.json \
  0.0.0.0:8080/countries/TET
