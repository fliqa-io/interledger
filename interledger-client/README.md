# Interledger Open payments API client

```shell
./gradlew generateAuth

./gradlew generateResource

./gradlew generateWallet
```

### NOTE:

Yaml have been modified because OpenAPI generator was unable to produced client  

Plus some additional clean up was performed so some models are not generated multiple time (for instance Amount) 

Some classes need to be manually fixed (enum problems)