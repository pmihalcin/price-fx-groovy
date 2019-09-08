package mihalcin

import static java.math.RoundingMode.HALF_UP

/**
 * Created by Patrik.Mihalcin on 4. 9. 2019
 */
class Main {

    static def calculateAverageProductPricePerGroup(List<Product> products, List<ProductCategory> categories, Map<String, String> margins) {

        // make the category look-up performance effective
        // treemap uses red-black tree, so lookup is O(log n)
        def intervalsAndCategories = new TreeMap<BigDecimal, String>()
        categories.each {
            intervalsAndCategories[new BigDecimal(it.lowerBound)] = it.categoryCode
        }
        // I made an assumption ranges are continuous, i.e. no gaps based on input data

        products.groupBy { it.groupCode }.collectEntries {
            def group = it.key
            def productsInGroup = it.value

            // calculate prices based on given formula
            def prices = productsInGroup.collect {
                def productCost = it.cost
                def category = intervalsAndCategories.floorEntry(productCost).value
                def margin = margins[category]
                def realMargin = margin.contains("%") ? margin.substring(0, margin.length() - 1).toBigDecimal() / 100.00G : margin.toBigDecimal()
                productCost * (1 + realMargin)
            }
            return [group, prices]
        }.collectEntries {
            def group = it.key
            def prices = it.value
            // reduce list to sum of list items and divide by size to get average
            def sum = prices.inject(0, { sum, value -> sum + value })
            BigDecimal avg = sum / prices.size()
            return [group, avg.setScale(1, HALF_UP)]
        }
    }

    static void main(String[] args) {
        def products = [
                ["A", "G1", 20.1],
                ["B", "G2", 98.4],
                ["C", "G1", 49.7],
                ["D", "G3", 35.8],
                ["E", "G3", 105.5],
                ["F", "G1", 55.2],
                ["G", "G1", 12.7],
                ["H", "G3", 88.6],
                ["I", "G1", 5.2],
                ["J", "G2", 72.4]]
        def transformedProducts = products.collect {
            new Product(it.get(0) as String, it.get(1) as String, it.get(2) as BigDecimal)
        }

        def categories = [
                ["C3", 50, 75],
                ["C4", 75, 100],
                ["C2", 25, 50],
                ["C5", 100, null],
                ["C1", 0, 25]]

        def transformedCategories = categories.collect {
            def lowerBound = it.get(1) as Integer
            new ProductCategory(it.get(0) as String, lowerBound)
        }

        def margins = [
                "C1": "20%",
                "C2": "30%",
                "C3": "0.4",
                "C4": "50%",
                "C5": "0.6"]

        def result = calculateAverageProductPricePerGroup(transformedProducts, transformedCategories, margins)

        assert result == [
                "G1": 37.5,
                "G2": 124.5,
                "G3": 116.1
        ]: "It doesn't work"

        println "It works!"
    }
}

class Product {
    String productCode
    String groupCode
    BigDecimal cost

    Product(String productCode, String groupCode, BigDecimal cost) {
        this.productCode = productCode
        this.groupCode = groupCode
        this.cost = cost
    }

    @Override
    String toString() {
        return "Product{" +
                "productCode='" + productCode + '\'' +
                ", groupCode='" + groupCode + '\'' +
                ", cost=" + cost +
                '}'
    }
}

class ProductCategory {
    String categoryCode
    Integer lowerBound

    ProductCategory(String categoryCode, Integer lowerBound) {
        this.categoryCode = categoryCode
        this.lowerBound = lowerBound
    }


    @Override
    String toString() {
        return "ProductCategory{" +
                "categoryCode='" + categoryCode + '\'' +
                ", lowerBound=" + lowerBound +
                '}'
    }
}
