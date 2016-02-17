# generic

Yleisiä java luokkia.

## DEPREKOITU

käytä java-utils moduulin alta sopivaa pakettia tämän sijaan

Jos käytät jotain täältä ja tarvitset muutoksia:
1. katso löytyykö valmiiksi sopiava java-utils alimoduuli [SRP](https://en.wikipedia.org/wiki/Single_responsibility_principle)
  * jos ei niin luo uusi
2. tee generics:stä ensin release versio (poista sen versionumerosta SNAPSHOT pääte ja pushaa)
2. päivitä uusi SNAPSHOT versio
3. *siirrä* luokka genericista java-utils submoodlin alle
4. tee muutokset
5. vaihda käyttävä projekti riippumaan java.utils:n alimoduulista ja tarvittaessa yhä genericin uudesta versiosta
