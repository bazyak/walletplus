package com.bazyak.walletplus.ui.utils

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex

/**
 * State for drag-and-drop reordering, similar to Android launcher.
 */
class DragDropState {
    // The index of the item currently being dragged
    var draggedIndex by mutableStateOf<Int?>(null)
        private set

    // Current drag offset from original position
    var dragOffset by mutableStateOf(Offset.Zero)
        private set

    // Whether currently over delete zone
    var isOverDeleteZone by mutableStateOf(false)
        private set

    // Current target index where item will be placed
    var targetIndex by mutableStateOf<Int?>(null)
        private set

    // Position of dragged item's center in screen coordinates
    private var draggedItemCenter by mutableStateOf(Offset.Zero)

    // Delete zone bounds
    private var deleteZoneBounds by mutableStateOf<androidx.compose.ui.geometry.Rect?>(null)

    // Item size (all items assumed same size in grid)
    private var itemSize by mutableStateOf(IntSize.Zero)

    // Grid positions map: index -> center position
    private val itemPositions = mutableMapOf<Int, Offset>()

    /**
     * Start dragging an item.
     */
    fun onDragStart(index: Int, startPosition: Offset) {
        draggedIndex = index
        dragOffset = Offset.Zero
        draggedItemCenter = startPosition
        targetIndex = index
        updateDeleteZoneCollision()
    }

    /**
     * Update drag position.
     */
    fun onDrag(offset: Offset) {
        dragOffset += offset
        draggedItemCenter += offset
        updateDeleteZoneCollision()
        targetIndex = calculateTargetIndex()
    }

    /**
     * Update dragged index (when list reorders during drag).
     */
    fun updateDraggedIndex(newIndex: Int) {
        draggedIndex = newIndex
    }

    /**
     * End dragging.
     */
    fun onDragEnd() {
        draggedIndex = null
        dragOffset = Offset.Zero
        draggedItemCenter = Offset.Zero
        targetIndex = null
        isOverDeleteZone = false
        itemPositions.clear()
    }

    /**
     * Update an item's position for reordering calculations.
     */
    fun updateItemPosition(index: Int, center: Offset, size: IntSize) {
        itemPositions[index] = center
        if (itemSize == IntSize.Zero) {
            itemSize = size
        }
    }

    /**
     * Calculate the current target index where the dragged item would be placed.
     */
    private fun calculateTargetIndex(): Int? {
        val currentIdx = draggedIndex ?: return null
        val draggedCenter = draggedItemCenter

        // Find closest item center
        var closestIndex = currentIdx
        var closestDistance = Float.MAX_VALUE

        for ((index, center) in itemPositions) {
            val distance = (draggedCenter - center).getDistanceSquared()
            if (distance < closestDistance) {
                closestDistance = distance
                closestIndex = index
            }
        }

        return closestIndex
    }

    /**
     * Update delete zone bounds.
     */
    fun updateDeleteZoneBounds(bounds: androidx.compose.ui.geometry.Rect) {
        deleteZoneBounds = bounds
        updateDeleteZoneCollision()
    }

    /**
     * Check if dragged item is over delete zone.
     */
    private fun updateDeleteZoneCollision() {
        val bounds = deleteZoneBounds
        isOverDeleteZone = draggedIndex != null && bounds != null && bounds.contains(draggedItemCenter)
    }

    val isDragging: Boolean
        get() = draggedIndex != null
}

@Composable
fun rememberDragDropState(): DragDropState = remember { DragDropState() }

/**
 * Modifier for draggable grid items.
 */
fun Modifier.draggableGridItem(
    state: DragDropState,
    index: Int,
    enabled: Boolean = true,
    onDragEnd: (fromIndex: Int, toIndex: Int?, overDeleteZone: Boolean) -> Unit,
): Modifier = this
    .onGloballyPositioned { coordinates ->
        val size = coordinates.size
        val center = coordinates.positionInRoot() + Offset(size.width / 2f, size.height / 2f)
        state.updateItemPosition(index, center, size)
    }
    .then(
        if (!enabled) {
            Modifier
        } else {
            Modifier.pointerInput(index) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f) + offset
                        state.onDragStart(index, center)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        state.onDrag(dragAmount)
                    },
                    onDragEnd = {
                        val target = state.targetIndex
                        val overDelete = state.isOverDeleteZone
                        state.onDragEnd()
                        onDragEnd(index, target, overDelete)
                    },
                    onDragCancel = {
                        state.onDragEnd()
                    },
                )
            }
        },
    )
    .zIndex(if (state.draggedIndex == index) 999f else 0f)
    .graphicsLayer {
        if (state.draggedIndex == index) {
            translationX = state.dragOffset.x
            translationY = state.dragOffset.y
            scaleX = 1.05f
            scaleY = 1.05f
            alpha = 0.9f
            shadowElevation = 16f
            compositingStrategy = CompositingStrategy.Offscreen
            // When floating, the card should look normal (override placeholder styling)
            // The graphicsLayer rendering will show the card as it would normally appear
        }
    }
