package com.github.secretx33.kotlinplayground

//import co.elastic.clients.elasticsearch.ElasticsearchClient
//import co.elastic.clients.json.jackson.JacksonJsonpMapper
//import co.elastic.clients.transport.rest_client.RestClientTransport
//import org.apache.http.HttpHost
//import org.apache.http.message.BasicHeader
//import org.elasticsearch.client.RestClient

// U2lndkY0c0JaVkFXa3ZfNHpmVzc6U1VVOXp0RnBUdHFDbi1QLW9aUDhKdw==

//private val elasticRestClient: RestClient by lazy {
//    RestClient.builder(HttpHost("9f388565c9094113927ba905121f5eae.us-central1.gcp.cloud.es.io", 443, "https"))
//        .setStrictDeprecationMode(true)
//        .setDefaultHeaders(arrayOf(
//            BasicHeader("Authorization", "ApiKey U2lndkY0c0JaVkFXa3ZfNHpmVzc6U1VVOXp0RnBUdHFDbi1QLW9aUDhKdw==")
//        )).build()
//}
//
//val elasticClient by lazy {
//    ElasticsearchClient(RestClientTransport(elasticRestClient, JacksonJsonpMapper(jackson)))
//}

//fun countEntityEntriesOfCompany(companyId: CompanyId, entityId: Long): Long {
//    val request = CountRequest.of {
//        it.index("company_${companyId}_entity_pt-br").query {
//            it.bool {
//                it.must {
//                    it.term {
//                        it.field("entityId").value(entityId)
//                    }
//                }
//            }
//        }
//    }
//    return elasticClient.count(request).count()
//}

//val activeCompanies = companyCollection.find(Filters.eq("parser.active", true)).mapTo(mutableSetOf()) { it.companyId }
//val ofActiveCompany = Filters.`in`("companyId", activeCompanies)
//val entityByCompany = entityV1Collection.find(and(ofActiveCompany, Filters.eq("active", true), Filters.eq("autocompleteActive", true))).groupBy { it.companyId }

//val entityEntriesInES = entityByCompany.mapValues { (companyId, entities) ->
//    entities.map {
//        log.info("Requesting count of company $companyId and entity ${it.entityId}")
//        countEntityEntriesOfCompany(companyId, it.entityId.toLong())
//    }
//}.filterValues { it.isNotEmpty() }

//val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.US))
//val data = """
//        === Entity entries in ElasticSearch ===
//
//        Total (in elasticsearch) = ${entityEntriesInES.values.sumOf { it.sum() }}
//
//        Maximum (per client) = ${entityEntriesInES.values.maxOf { it.sum() }}
//        Average (per client) = ${decimalFormat.format(entityEntriesInES.values.map { it.sum() }.average())}
//        Minimum (per client) = ${entityEntriesInES.values.minOf { it.sum() }}
//
//        Maximum (per entity type) = ${entityEntriesInES.values.flatten().max()}
//        Average (per entity type) = ${decimalFormat.format(entityEntriesInES.values.flatten().average())}
//        Minimum (per entity type) = ${entityEntriesInES.values.flatten().min()}
//    """.trimIndent()
//log.info(data)

//fun search(companyId: CompanyId, query: String): SearchResponse<Map<*, *>> {
//    val request = SearchRequest.of {
//        it.index("company_${companyId}_entity_pt-br").query {
//            it.term {
//                it.field("companyId").value(companyId.toLong())
//            }
//            it.simpleQueryString(SimpleQueryStringQuery.of {
//                it.query(query)
//            })
//        }
//    }
//    return elasticClient.search(request, Map::class.java)
//}