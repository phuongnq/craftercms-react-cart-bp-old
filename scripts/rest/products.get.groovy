import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.FieldSortBuilder
import org.elasticsearch.search.sort.SortOrder

def result = [:]

def queryStatement = 'content-type:"/component/product"'

def builder = new SearchSourceBuilder().query(QueryBuilders.queryStringQuery(queryStatement))

// execute query
def executedQuery = elasticsearch.search(new SearchRequest().source(builder))

result.products = []

def elasticResults = executedQuery.hits.hits*.getSourceAsMap()
elasticResults.eachWithIndex { document, idx ->
	def product = [ id:             idx,
    				cmsId:          document.localId,
    				sku:            document.sKU_s, 
                    title:          document.title_s, 
                    style:          document.style_s,
                    description:    document.description_t,
                    price:          getPrice(document),        // potentially get the price from external system
                    installments:   getInventory(document),    // potentially get inventory from external system
                    isFreeShipping: document.freeShipping_s,
                    availableSizes: getAvailableSizes(document),
                    currencyId:     "USD",  // hard code USD for now
                    currencyFormat: "\$",   // hard code currency format for now
                  ]
    
	result.products.add(product)
}

return result

def getAvailableSizes(document) {
    def sizes = []
    
    // HashMap
    if (document.sizes_o.item.getClass() == HashMap) {
        sizes.add(document.sizes_o.item.key)
        return sizes
    }

    // ArrayList
    document.sizes_o.item.eachWithIndex { elm, idx ->
        sizes.add(elm.get("key"))
    }
    
    return sizes
}


def getPrice(document) {
	// simple example of abstracting where price comes from
	return new Float(document.price_s)
}

def getInventory(document) {
	// simple example of abstracting where inventory comes from
	return new Integer(document.installments_s)
}