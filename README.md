# generic

Yleisiä java luokkia.

## DEPREKOITU

käytä [java-utils](https://github.com/Opetushallitus/java-utils) moduulin alta sopivaa pakettia tämän sijaan

Jos käytät jotain täältä ja tarvitset muutoksia:

1. katso löytyykö valmiiksi sopiava java-utils alimoduuli [SRP](https://en.wikipedia.org/wiki/Single_responsibility_principle)
 * jos ei niin luo uusi
2. tee generics:stä ensin release versio (poista sen versionumerosta SNAPSHOT pääte ja pushaa)
2. päivitä uusi SNAPSHOT versio
3. *siirrä* luokka genericista java-utils submoodlin alle
4. tee muutokset
5. vaihda käyttävä projekti riippumaan java.utils:n alimoduulista ja tarvittaessa yhä genericin uudesta versiosta

Muutoslokia:

9.6-SNAPSHOT

* Poistetaan vanha `CustomCasAuthenticationFilter`, sen korvaa [OpintopolkuCasAuthenticationFilter](https://github.com/Opetushallitus/java-utils/blob/5d90fbf956f9b530770fadeff1dcc72937e52dc5/opintopolku-cas-servlet-filter/src/main/java/fi/vm/sade/java_utils/security/OpintopolkuCasAuthenticationFilter.java)
* Poistetaan vanha `generic-vaadin-widgetset` -moduli

9.7-SNAPSHOT

* Siirretty OrganisationHierarchyAuthorizer java-utilsiin
 * Poistettu siihen liittyvät luokat
 * Java-utilsiin ei lisätty Spring-tukea. Jos se tarvitaan, pitää tehdä oma moduuli, jotta Spring ei valu esim. valinta-tulos-serviceen

* Poistetaan `/generic-common/pom.xml`:stä dependenssi `jgroups`.
* Poistetaan luokat `JGroupsCustomLog` ja `GroupsLoggerFactory`

9.9-SNAPSHOT
* Siirretään java-utilsiin seuraavat luokat (uusi java-utilsin alimoduli suluissa perässä)
  * SimpleCache (java-cache)
  * CachingRestClient (legacy-caching-rest-client)
  * CasApplicationAsAUserInterceptor , DefaultTicketCachePolicy , TicketCachePolicy , PERA , ProxyAuthenticator (legacy-cxf-cas)
  * Authorizer , NotAuthorizedException , SadeBusinessException , OidProvider , OrganisationHierarchyAuthorizer , ThreadLocalAuthorizer (opintopolku-spring-security)
  * BuildVersionHealthChecker , DatabaseHealthChecker , ProxyAuthenticationChecker , SpringAwareHealthCheckServlet (spring-aware-health-check-servlet)
  * HealthChecker (opintopolku-healthcheck-api)
  * TraceInterceptor (java-tracing)
* Poistetaan AuthorizationUtil , jonka toiminnallisuus on nyt suoraan sitä käyttävissä valintalaskentakoostepalvelu- ja valintalaskenta-service -moduleissa
* Poistetaan seuraavat luokat, jotka eivät ole enää käytössä:
  * CachingHttpGetClient
  * Cacheable
  * CacheableJerseyFilter
  * Role
