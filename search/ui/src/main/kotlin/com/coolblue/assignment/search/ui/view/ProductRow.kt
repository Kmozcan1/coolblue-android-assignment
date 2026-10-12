package com.coolblue.assignment.search.ui.view

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.coolblue.assignment.core.ui.theme.CoolblueColors
import com.coolblue.assignment.search.ui.R
import com.coolblue.assignment.search.ui.model.ProductUiModel

// Every CDN image is 300×750 on white with the product centred and filling the width. A 1:2 tile
// crops the empty top and bottom tenths and still shows each product whole, bar the Pencil's ends.
private val TileWidth = 84.dp
private val TileHeight = 168.dp
private val TileShape = RoundedCornerShape(8.dp)

/** The row shows this many USPs; the detail sheet shows them all. */
private const val ROW_USPS = 3

@Composable
internal fun ProductRow(
    product: ProductUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // clickable merges the descendants: one TalkBack node per product, "double-tap to show details".
            .clickable(onClickLabel = stringResource(R.string.show_details), onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ProductTile(imageUrl = product.imageUrl, modifier = Modifier.size(TileWidth, TileHeight))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            product.promoText?.let { PromoKicker(it, Modifier.padding(top = 2.dp)) }
            Rating(rating = product.rating, reviewCount = product.reviewCount, modifier = Modifier.padding(top = 6.dp))
            Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                product.usps.take(ROW_USPS).forEach { Usp(it, singleLine = true) }
            }
            Price(price = product.price, listPrice = product.listPrice, modifier = Modifier.padding(top = 12.dp))
            if (product.nextDayDelivery) Delivery(Modifier.padding(top = 2.dp))
        }
    }
}

/** The bottom-sheet body: the same product, nothing clipped, every USP. */
@Composable
internal fun ProductDetail(
    product: ProductUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth() // so the tile centres in the sheet, not in a column as wide as the name
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
    ) {
        // Twice the row tile. The source is 300 px wide, so this is as large as it goes before it softens.
        // In a short window (phone landscape) the row size, or the name would start below the fold.
        val compactHeight = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() } < 480.dp
        val scale = if (compactHeight) 1 else 2
        ProductTile(
            imageUrl = product.imageUrl,
            modifier = Modifier
                .size(TileWidth * scale, TileHeight * scale)
                .align(Alignment.CenterHorizontally),
        )
        Text(
            text = product.name,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 20.dp),
        )
        product.promoText?.let { PromoKicker(it, Modifier.padding(top = 4.dp)) }
        Rating(rating = product.rating, reviewCount = product.reviewCount, modifier = Modifier.padding(top = 8.dp))
        Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            product.usps.forEach { Usp(it, singleLine = false) }
        }
        Price(price = product.price, listPrice = product.listPrice, modifier = Modifier.padding(top = 16.dp))
        if (product.nextDayDelivery) Delivery(Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ProductTile(imageUrl: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = null, // decorative: the name is read as text
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(TileShape)
            .background(CoolblueColors.Plate)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, TileShape),
    )
}

@Composable
private fun PromoKicker(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary,
        modifier = modifier,
    )
}

@Composable
private fun Rating(rating: String, reviewCount: Int, modifier: Modifier = Modifier) {
    val reviews = pluralStringResource(R.plurals.review_count, reviewCount, reviewCount)
    // Read as one phrase with its meaning: the bare "9,1" would otherwise arrive without context.
    val label = stringResource(R.string.rating_a11y, rating, reviews)
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = label },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(14.dp),
        )
        Text(text = rating, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(
            text = reviews,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 2.dp),
        )
    }
}

@Composable
private fun Usp(text: String, singleLine: Boolean) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp).size(14.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (singleLine) 1 else Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun Price(price: String, listPrice: String?, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = price,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.alignByBaseline(),
        )
        if (listPrice != null) {
            // The strike-through has no semantics; say what the figure is so it isn't read as a second price.
            val wasLabel = stringResource(R.string.list_price_a11y, listPrice)
            Text(
                text = listPrice,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough,
                modifier = Modifier
                    .alignByBaseline()
                    .semantics { contentDescription = wasLabel },
            )
        }
    }
}

@Composable
private fun Delivery(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.delivered_tomorrow),
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.tertiary,
        modifier = modifier,
    )
}

/** A row-shaped placeholder for a page in flight: the geometry of [ProductRow], no text, a slow pulse. */
@Composable
internal fun ProductRowSkeleton(modifier: Modifier = Modifier) {
    val pulse by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 800, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    val bar = MaterialTheme.colorScheme.surfaceContainerHigh
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .alpha(pulse),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(Modifier.size(TileWidth, TileHeight).background(bar, TileShape))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Bar(bar, widthFraction = 0.85f, height = 16.dp)
            Bar(bar, widthFraction = 0.55f, height = 16.dp)
            Bar(bar, widthFraction = 0.35f, height = 12.dp)
            Spacer(Modifier.height(2.dp))
            Bar(bar, widthFraction = 0.6f, height = 10.dp)
            Bar(bar, widthFraction = 0.7f, height = 10.dp)
            Bar(bar, widthFraction = 0.4f, height = 10.dp)
            Spacer(Modifier.height(6.dp))
            Bar(bar, widthFraction = 0.3f, height = 22.dp)
        }
    }
}

@Composable
private fun Bar(color: Color, widthFraction: Float, height: Dp) {
    Box(Modifier.fillMaxWidth(widthFraction).height(height).background(color, RoundedCornerShape(4.dp)))
}
