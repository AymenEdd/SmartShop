package com.example.smartshop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartshop.network.CartItem

@Composable
internal fun CartItemRow(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSoftColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    item.product.name,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${"%.2f".format(item.product.price)} EUR",
                    color = AccentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = onDecrement,
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp)
                ) { Text("−") }
                Text(
                    "${item.quantity}",
                    modifier = Modifier.padding(horizontal = 6.dp),
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onIncrement,
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp)
                ) { Text("+") }
            }
            Button(
                onClick = onRemove,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRedColor.copy(alpha = 0.1f),
                    contentColor = ErrorRedColor
                )
            ) { Text("Remove", fontSize = 12.sp) }
        }
    }
}