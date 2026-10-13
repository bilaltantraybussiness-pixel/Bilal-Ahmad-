package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BookPurchaseEntity
import com.example.data.model.KashmiriContributionEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.LocalizationManager
import com.example.viewmodel.RozgarViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KashmiriLiteratureScreen(
    viewModel: RozgarViewModel,
    onNavigateBack: () -> Unit = { viewModel.navigateBack() }
) {
    val currentLang by LocalizationManager.currentLanguage.collectAsState()
    val allContributions by viewModel.kashmiriContributions.collectAsState()
    val userPurchases by viewModel.userBookPurchases.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Books, 1: Articles & Stories, 2: Poetry, 3: Publish
    var selectedGenre by remember { mutableStateOf("All Genres") }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog state for reading or purchasing
    var activeReadingItem by remember { mutableStateOf<KashmiriContributionEntity?>(null) }
    var activePurchasingBook by remember { mutableStateOf<KashmiriContributionEntity?>(null) }

    val books = remember(allContributions, selectedGenre, searchQuery) {
        allContributions.filter { it.isBook }.filter { item ->
            (selectedGenre == "All Genres" || item.genre.contains(selectedGenre.take(5), ignoreCase = true)) &&
            (searchQuery.isBlank() || item.titleKashmiri.contains(searchQuery, ignoreCase = true) || item.titleEnglish.contains(searchQuery, ignoreCase = true) || item.authorName.contains(searchQuery, ignoreCase = true))
        }
    }

    val articlesAndStories = remember(allContributions, searchQuery) {
        allContributions.filter { !it.isBook && (it.type == "ARTICLE" || it.type == "STORY" || it.type == "FOLKLORE" || it.type == "RESEARCH") }.filter { item ->
            searchQuery.isBlank() || item.titleKashmiri.contains(searchQuery, ignoreCase = true) || item.titleEnglish.contains(searchQuery, ignoreCase = true) || item.authorName.contains(searchQuery, ignoreCase = true)
        }
    }

    val poetry = remember(allContributions, searchQuery) {
        allContributions.filter { !it.isBook && (it.type == "POETRY" || it.genre.contains("Poetry", ignoreCase = true) || it.genre.contains("شٲعِری")) }.filter { item ->
            searchQuery.isBlank() || item.titleKashmiri.contains(searchQuery, ignoreCase = true) || item.titleEnglish.contains(searchQuery, ignoreCase = true) || item.authorName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentLang == AppLanguage.URDU) "کشمیری زبان و کتب"
                            else if (currentLang == AppLanguage.KASHMIRI) "کٲشِر زَبانہِ ہِنٛز خِدمَت تہٕ کِتابہٕ"
                            else "Contribute to Kashmiri Language",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Preserving Literature, Poetry, Stories & Books",
                            style = MaterialTheme.typography.labelSmall.copy(color = KashmirMint, fontSize = 11.sp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KashmirDeepGreen,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(KashmirBackground)
                .padding(padding)
        ) {
            // Hero Kashmiri Language Preservation Banner
            Surface(
                color = KashmirForestGreen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(KashmirGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = KashmirDeepGreen, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "کٲشِر زَبانہِ ہِنٛز خِدمَت • کتب بازار",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KashmirGold
                        )
                        Text(
                            text = "Publish your writings, buy Kashmiri books, and support writers of the Valley.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Tab Navigation Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = KashmirDeepGreen,
                divider = { HorizontalDivider(color = KashmirCardBorder) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("📚 Books", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("✍️ Articles", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("📜 Poetry", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("➕ Publish", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KashmirEmerald) }
                )
            }

            // Content Section
            when (selectedTab) {
                0 -> {
                    // Books Marketplace Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        item {
                            // Search Box
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search Kashmiri books, authors, or genres...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = KashmirEmerald) },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("book_search_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Genre Filter Chips
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(KASHMIRI_GENRES) { genre ->
                                    val isSelected = selectedGenre == genre
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedGenre = genre },
                                        label = { Text(genre, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = KashmirDeepGreen,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (allContributions.isEmpty()) {
                            items(3) {
                                LiteratureBookSkeleton()
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        } else if (books.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = "No Books Found",
                                    message = "Be the first to publish a Kashmiri book or essay in this category!",
                                    icon = Icons.Default.AutoStories,
                                    actionText = "Publish Your Book",
                                    onAction = { selectedTab = 3 }
                                )
                            }
                        } else {
                            items(books) { book ->
                                val isPurchased = userPurchases.any { it.bookId == book.id }
                                BookMarketplaceCard(
                                    book = book,
                                    isPurchased = isPurchased,
                                    onReadClick = {
                                        viewModel.recordBookRead(book.id)
                                        activeReadingItem = book
                                    },
                                    onBuyClick = {
                                        activePurchasingBook = book
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }

                1 -> {
                    // Articles & Stories Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(articlesAndStories) { item ->
                            WritingItemCard(
                                item = item,
                                onRead = {
                                    viewModel.recordBookRead(item.id)
                                    activeReadingItem = item
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                2 -> {
                    // Poetry Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(poetry) { poem ->
                            PoetryItemCard(
                                poem = poem,
                                onRead = {
                                    viewModel.recordBookRead(poem.id)
                                    activeReadingItem = poem
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                3 -> {
                    // Publish & Contribute Form
                    PublishContributionForm(
                        viewModel = viewModel,
                        onPublished = {
                            selectedTab = 0
                        }
                    )
                }
            }
        }
    }

    // Reader Dialog
    activeReadingItem?.let { readingItem ->
        BookReaderDialog(
            item = readingItem,
            onDismiss = { activeReadingItem = null }
        )
    }

    // Purchase Dialog
    activePurchasingBook?.let { bookToBuy ->
        BookPurchaseDialog(
            book = bookToBuy,
            viewModel = viewModel,
            onDismiss = { activePurchasingBook = null },
            onPurchased = {
                activePurchasingBook = null
                activeReadingItem = bookToBuy
            }
        )
    }
}

@Composable
fun BookMarketplaceCard(
    book: KashmiriContributionEntity,
    isPurchased: Boolean,
    onReadClick: () -> Unit,
    onBuyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("book_card_${book.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book Cover Art / Spine
            Box(
                modifier = Modifier
                    .width(76.dp)
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(KashmirForestGreen),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(6.dp)
                ) {
                    Icon(Icons.Default.MenuBook, null, tint = KashmirGold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.titleKashmiri.take(18),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Book Information
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = KashmirMint,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = book.genre,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (book.isPaid) {
                        Text(
                            text = if (isPurchased) "✓ Purchased" else "₹${book.price.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = if (isPurchased) KashmirEmerald else KashmirForestGreen
                        )
                    } else {
                        Text("Free", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KashmirEmerald)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title in authentic Kashmiri / Nastaliq script
                Text(
                    text = book.titleKashmiri,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = KashmirTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (book.titleEnglish.isNotBlank()) {
                    Text(
                        text = book.titleEnglish,
                        fontSize = 12.sp,
                        color = KashmirTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "By ${book.authorName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = KashmirEmerald
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${book.pageCount} Pages • ${book.readsCount} Readers",
                        fontSize = 11.sp,
                        color = KashmirTextSecondary
                    )

                    if (!book.isPaid || isPurchased) {
                        Button(
                            onClick = onReadClick,
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Read Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onBuyClick,
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirForestGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buy Book", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WritingItemCard(
    item: KashmiriContributionEntity,
    onRead: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = KashmirBackground,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(item.genre, fontSize = 10.sp, color = KashmirTextSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Text("By ${item.authorName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KashmirEmerald)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(item.titleKashmiri, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KashmirDeepGreen)
            if (item.titleEnglish.isNotBlank()) {
                Text(item.titleEnglish, fontSize = 12.sp, color = KashmirTextSecondary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.summary.ifBlank { item.content.take(120) + "..." },
                fontSize = 12.sp,
                color = KashmirTextPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${item.readsCount} Reads", fontSize = 11.sp, color = KashmirTextSecondary)
                TextButton(onClick = onRead) {
                    Text("Read Full Article →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KashmirEmerald)
                }
            }
        }
    }
}

@Composable
fun PoetryItemCard(
    poem: KashmiriContributionEntity,
    onRead: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirGold.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("📜 شٲعِری", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KashmirGold)
                Text(poem.authorName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KashmirForestGreen)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(poem.titleKashmiri, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KashmirDeepGreen, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = KashmirBackground,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = poem.content.take(180) + "...",
                    fontSize = 13.sp,
                    color = KashmirTextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(onClick = onRead) {
                Text("Read Full Poem & Verses", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KashmirEmerald)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishContributionForm(
    viewModel: RozgarViewModel,
    onPublished: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()

    var type by remember { mutableStateOf("Book") }
    var titleKashmiri by remember { mutableStateOf("") }
    var titleEnglish by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var authorBio by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("Poetry (شٲعِری)") }
    var content by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var isPaid by remember { mutableStateOf(false) }
    var priceText by remember { mutableStateOf("49") }
    var pageCountText by remember { mutableStateOf("25") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, KashmirCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Publish to Kashmiri Literature & Bookstore",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                    )
                    Text(
                        text = "Contribute your writing, poetry, articles, or publish an original book to earn from readers.",
                        fontSize = 11.sp,
                        color = KashmirTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Type Selector
                    Text("Select Content Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("Book", "Poetry", "Article", "Short Story", "Folklore", "Sufism")) { itemType ->
                            val isSelected = type == itemType
                            FilterChip(
                                selected = isSelected,
                                onClick = { type = itemType },
                                label = { Text(itemType, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Kashmiri Title
                    OutlinedTextField(
                        value = titleKashmiri,
                        onValueChange = { titleKashmiri = it },
                        label = { Text("Title in Kashmiri / Urdu Script *") },
                        placeholder = { Text("e.g. لال دؠد ہِنٛز شٲعِری or کٲشُر افسانہٕ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // English Title
                    OutlinedTextField(
                        value = titleEnglish,
                        onValueChange = { titleEnglish = it },
                        label = { Text("Title in English (Optional)") },
                        placeholder = { Text("e.g. Poetry of Lal Ded or Kashmir Folk Tales") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Author Name & Bio
                    OutlinedTextField(
                        value = authorName,
                        onValueChange = { authorName = it },
                        label = { Text("Author / Contributor Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = authorBio,
                        onValueChange = { authorBio = it },
                        label = { Text("Author Profile Bio (Optional)") },
                        placeholder = { Text("e.g. Kashmiri researcher and poet from Baramulla") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Genre Selector
                    OutlinedTextField(
                        value = genre,
                        onValueChange = { genre = it },
                        label = { Text("Genre / Category") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Paid / Free Switch
                    if (type == "Book") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Monetize Book (Paid Access)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Set a price for readers to buy and download", fontSize = 10.sp, color = KashmirTextSecondary)
                            }
                            Switch(
                                checked = isPaid,
                                onCheckedChange = { isPaid = it }
                            )
                        }

                        if (isPaid) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = priceText,
                                    onValueChange = { priceText = it },
                                    label = { Text("Price (₹)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = pageCountText,
                                    onValueChange = { pageCountText = it },
                                    label = { Text("Pages") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Content text
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Content / Excerpt / Manuscript *") },
                        placeholder = { Text("Type or paste Kashmiri text, verses, or book chapter sample here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage ?: "", color = KashmirError, fontSize = 12.sp)
                    }

                    if (successMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(successMessage ?: "", color = KashmirDeepGreen, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (titleKashmiri.isBlank() || authorName.isBlank() || content.isBlank()) {
                                errorMessage = "Please fill in title, author name, and content text."
                                return@Button
                            }
                            isSubmitting = true
                            errorMessage = null

                            viewModel.publishKashmiriContribution(
                                type = type.uppercase(),
                                titleKashmiri = titleKashmiri.trim(),
                                titleEnglish = titleEnglish.trim(),
                                authorName = authorName.trim(),
                                authorBio = authorBio.trim(),
                                genre = genre.trim(),
                                content = content.trim(),
                                summary = summary.trim(),
                                isBook = type.equals("Book", ignoreCase = true),
                                isPaid = isPaid,
                                price = if (isPaid) priceText.toDoubleOrNull() ?: 49.0 else 0.0,
                                pageCount = pageCountText.toIntOrNull() ?: 1
                            ) { success, msg ->
                                isSubmitting = false
                                if (success) {
                                    successMessage = "Published successfully! Available in library."
                                    onPublished()
                                } else {
                                    errorMessage = msg
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Publish Literature / Book", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookReaderDialog(
    item: KashmiriContributionEntity,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.titleKashmiri, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = KashmirDeepGreen)
                        Text("Author: ${item.authorName}", fontSize = 12.sp, color = KashmirEmerald)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(min = 200.dp, max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = item.content,
                        fontSize = 15.sp,
                        lineHeight = 26.sp,
                        color = KashmirTextPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${item.readsCount} Views", fontSize = 12.sp, color = KashmirTextSecondary)
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Close Reader")
                    }
                }
            }
        }
    }
}

@Composable
fun BookPurchaseDialog(
    book: KashmiriContributionEntity,
    viewModel: RozgarViewModel,
    onDismiss: () -> Unit,
    onPurchased: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    var upiRef by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Purchase Kashmiri Book", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KashmirDeepGreen)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = KashmirMint,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(book.titleKashmiri, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KashmirDeepGreen)
                        Text("By ${book.authorName}", fontSize = 12.sp, color = KashmirTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Price:", fontSize = 13.sp, color = KashmirTextSecondary)
                            Text("₹${book.price.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = KashmirForestGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Instant digital download and reader access will be unlocked upon payment confirmation.",
                    fontSize = 11.sp,
                    color = KashmirTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = upiRef,
                    onValueChange = { upiRef = it },
                    label = { Text("Payment UPI Ref / UTR (Optional)") },
                    placeholder = { Text("e.g. 428190001234") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMessage ?: "", color = KashmirError, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isProcessing = true
                        viewModel.purchaseBook(
                            book = book,
                            paymentRef = upiRef.ifBlank { "UPI_${System.currentTimeMillis()}" }
                        ) { success, msg ->
                            isProcessing = false
                            if (success) {
                                onPurchased()
                            } else {
                                errorMessage = msg
                            }
                        }
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirForestGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Confirm Purchase (₹${book.price.toInt()})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
