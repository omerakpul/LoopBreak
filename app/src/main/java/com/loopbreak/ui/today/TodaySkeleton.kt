package com.loopbreak.ui.today

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.loopbreak.R
import com.loopbreak.ui.components.ShadCard
import com.loopbreak.ui.components.ShadSkeleton
import com.loopbreak.ui.components.describe
import com.loopbreak.ui.components.heroBrush

/** Placeholder cards shown while the very first sync is still collecting data. */
fun LazyListScope.todaySkeleton() {
    item {
        ShadCard(modifier = Modifier.describe(stringResource(R.string.loading)), brush = heroBrush()) {
            ShadSkeleton(Modifier.width(140.dp).height(14.dp))
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ShadSkeleton(Modifier.size(220.dp), CircleShape)
            }
            Spacer(Modifier.height(16.dp))
            ShadSkeleton(Modifier.fillMaxWidth().height(8.dp), RoundedCornerShape(50))
        }
    }
    items(2) {
        ShadCard {
            ShadSkeleton(Modifier.width(180.dp).height(16.dp))
            repeat(3) {
                Spacer(Modifier.height(12.dp))
                ShadSkeleton(Modifier.fillMaxWidth().height(36.dp))
            }
        }
    }
}
