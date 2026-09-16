package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ProductEntity
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.components.ProductCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.IlamViewModel

@Composable
fun CustomerShopScreen(
    viewModel: IlamViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val publishedProducts by viewModel.publishedProducts.collectAsState()
    val categories by viewModel.visibleCategories.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val wishlist by viewModel.rawWishlist.collectAsState()

    val wishlistedIds = remember(wishlist) { wishlist.map { it.productId }.toSet() }

    var selectedFilterTag by remember { mutableStateOf("ALL") } // ALL, NEW, BESTSELLER, SALE

    // Filter products dynamically
    val filteredProducts = remember(publishedProducts, selectedCategoryId, searchQuery, selectedFilterTag, language) {
        publishedProducts.filter { product ->
            val matchesCategory = selectedCategoryId == null || product.categoryId == selectedCategoryId
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                product.nameEn.contains(searchQuery, ignoreCase = true) ||
                        product.nameBn.contains(searchQuery, ignoreCase = true) ||
                        product.descEn.contains(searchQuery, ignoreCase = true) ||
                        product.sku.contains(searchQuery, ignoreCase = true)
            }
            val matchesTag = when (selectedFilterTag) {
                "NEW" -> product.isNewArrival
                "BESTSELLER" -> product.isBestSeller
                "SALE" -> product.isSale || product.discountPrice > 0
                else -> true
            }

            matchesCategory && matchesSearch && matchesTag
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IlamOffWhite)
            .testTag("customer_shop_screen")
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("shop_search_input"),
            placeholder = {
                Text(
                    text = Strings.searchPlaceholder(language),
                    fontSize = 13.sp,
                    color = IlamTextMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = IlamTextSecondary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = IlamTextSecondary)
                    }
                }
            },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = IlamWhite,
                unfocusedContainerColor = IlamWhite,
                focusedBorderColor = IlamBlack,
                unfocusedBorderColor = IlamBorderLight
            ),
            singleLine = true
        )

        // Category Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { viewModel.selectCategoryFilter(null) },
                    label = {
                        Text(
                            text = Strings.allProducts(language),
                            fontSize = 12.sp,
                            fontWeight = if (selectedCategoryId == null) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IlamBlack,
                        selectedLabelColor = IlamWhite,
                        containerColor = IlamWhite,
                        labelColor = IlamBlack
                    )
                )
            }

            items(categories) { category ->
                val isSelected = selectedCategoryId == category.id
                val catName = if (language == AppLanguage.BN && category.nameBn.isNotBlank()) category.nameBn else category.nameEn

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectCategoryFilter(category.id) },
                    label = {
                        Text(
                            text = catName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IlamBlack,
                        selectedLabelColor = IlamWhite,
                        containerColor = IlamWhite,
                        labelColor = IlamBlack
                    )
                )
            }
        }

        // Tag Filters (All, New, Best Seller, Sale)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tags = listOf(
                "ALL" to if (language == AppLanguage.BN) "সব" else "All",
                "NEW" to if (language == AppLanguage.BN) "নতুন" else "New",
                "BESTSELLER" to if (language == AppLanguage.BN) "সেরা" else "Best",
                "SALE" to if (language == AppLanguage.BN) "ছাড়" else "Sale"
            )

            tags.forEach { (tagKey, tagLabel) ->
                val isSelected = selectedFilterTag == tagKey
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) IlamGoldDark else IlamWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) IlamGoldDark else IlamBorderLight),
                    modifier = Modifier.clickable { selectedFilterTag = tagKey }
                ) {
                    Text(
                        text = tagLabel,
                        color = if (isSelected) IlamWhite else IlamTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Results count
        Text(
            text = "${filteredProducts.size} ${if (language == AppLanguage.BN) "টি পণ্য পাওয়া গেছে" else "pieces found"}",
            color = IlamTextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Products Grid
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (language == AppLanguage.BN) "কোন পণ্য পাওয়া যায়নি" else "No garments match your filter",
                        color = IlamBlack,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.setSearchQuery("")
                            viewModel.selectCategoryFilter(null)
                            selectedFilterTag = "ALL"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IlamBlack)
                    ) {
                        Text(text = if (language == AppLanguage.BN) "ফিল্টার মুছুন" else "Clear Filters")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 84.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        language = language,
                        isWishlisted = wishlistedIds.contains(product.id),
                        onProductClick = { viewModel.selectProduct(product) },
                        onWishlistToggle = { viewModel.toggleWishlist(product.id) }
                    )
                }
            }
        }
    }
}
