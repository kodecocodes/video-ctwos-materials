/*
 * Copyright (c) 2021 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.wearostile

import androidx.core.content.ContextCompat
import androidx.wear.tiles.TileProviderService
import androidx.wear.tiles.builders.*
import androidx.wear.tiles.builders.DimensionBuilders.dp
import androidx.wear.tiles.readers.DeviceParametersReaders
import androidx.wear.tiles.readers.RequestReaders
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.guava.future

class WaterService(private val waterRepository: WaterRepository = WaterRepository()): TileProviderService(){

  private val ioScope = CoroutineScope(Dispatchers.IO)

  private val ID_IMAGE_DRINK = "image_drink"
  private val ID_IMAGE_PLUS_ONE = "image_plus_one"
  private val ID_IMAGE_MINUS_ONE = "image_minus_one"

  override fun onTileRequest(requestParams: RequestReaders.TileRequest) = ioScope.future {
    val deviceParams = requestParams.deviceParameters

    when(requestParams.state.lastClickableId) {
      ID_IMAGE_PLUS_ONE -> waterRepository.incrementFullGlass()
      ID_IMAGE_MINUS_ONE -> waterRepository.decrementFullGlass()
    }

    val glassCount = waterRepository.getGlassCount()

    TileBuilders.Tile.builder()
      .setResourcesVersion("1")
      .setTimeline(
        TimelineBuilders.Timeline.builder()
          .addTimelineEntry(
            TimelineBuilders.TimelineEntry.builder().setLayout(
              LayoutElementBuilders.Layout.builder().setRoot(
                layout(glassCount, deviceParams)
              )
            )
          )
      )
      .build()
  }

  private fun layout(glassCount: Float, deviceParameters: DeviceParametersReaders.DeviceParameters) =
    LayoutElementBuilders.Column.builder()
      .addContent(
        LayoutElementBuilders.Row.builder()
          .addContent(currentGlassText(glassCount.toString(), deviceParameters))
          .addContent(LayoutElementBuilders.Spacer.builder().setWidth(dp(16f)))
          .addContent(glassIcon())
      )
      .addContent(LayoutElementBuilders.Spacer.builder().setHeight(dp(12f)))
      .addContent(
        LayoutElementBuilders.Row.builder()
          .addContent(imageButton(ID_IMAGE_MINUS_ONE))
          .addContent(LayoutElementBuilders.Spacer.builder().setWidth(dp(24f)))
          .addContent(imageButton(ID_IMAGE_PLUS_ONE))
      )

  private fun currentGlassText(current: String, deviceParameters: DeviceParametersReaders.DeviceParameters) = LayoutElementBuilders.Text.builder()
    .setText(current)
    .setFontStyle(LayoutElementBuilders.FontStyles.display2(deviceParameters))
    .build()

  private fun glassIcon() =
    LayoutElementBuilders.Image.builder()
      .setWidth(dp(40f))
      .setHeight(dp(40f))
      .setResourceId(ID_IMAGE_DRINK)
      .build()

  private fun imageButton(id: String) =
    LayoutElementBuilders.Image.builder()
      .setWidth(dp(32f))
      .setHeight(dp(32f))
      .setResourceId(id)
      .setModifiers(
        ModifiersBuilders.Modifiers.builder()
          .setBackground(
            ModifiersBuilders.Background.builder()
              .setColor(
                ColorBuilders.argb(ContextCompat.getColor(this, R.color.colorPrimary))
              )
              .setCorner(ModifiersBuilders.Corner.builder().setRadius(dp(16f)).build())
              .build()
          )
          .setPadding(
            ModifiersBuilders.Padding.builder()
              .setAll(dp(4f))
              .build()
          )
          .setClickable(
            ModifiersBuilders.Clickable.builder()
              .setId(id)
              .setOnClick(ActionBuilders.LoadAction.builder())
          )
      )
      .build()

  override fun onResourcesRequest(
    requestParams: RequestReaders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
    return ioScope.future {
      ResourceBuilders.Resources.builder()
        .setVersion("1")
        .addIdToImageMapping(
          ID_IMAGE_DRINK,
          ResourceBuilders.ImageResource.builder()
            .setAndroidResourceByResId(
              ResourceBuilders.AndroidImageResourceByResId.builder()
                .setResourceId(R.drawable.ic_drink)
            )
        )
        .addIdToImageMapping(
          ID_IMAGE_MINUS_ONE,
          ResourceBuilders.ImageResource.builder()
            .setAndroidResourceByResId(
              ResourceBuilders.AndroidImageResourceByResId.builder()
                .setResourceId(R.drawable.ic_minus_one)
            )
        )
        .addIdToImageMapping(
          ID_IMAGE_PLUS_ONE,
          ResourceBuilders.ImageResource.builder()
            .setAndroidResourceByResId(
              ResourceBuilders.AndroidImageResourceByResId.builder()
                .setResourceId(R.drawable.ic_plus_one)
            )
        )
        .build()
    }
  }


  override fun onDestroy() {
    super.onDestroy()
    ioScope.cancel()
  }
}