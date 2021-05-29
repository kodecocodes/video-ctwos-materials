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

import androidx.wear.tiles.TileProviderService
import androidx.wear.tiles.builders.*
import androidx.wear.tiles.readers.DeviceParametersReaders
import androidx.wear.tiles.readers.RequestReaders
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.guava.future

class WaterService(private val waterRepository: WaterRepository = WaterRepository()): TileProviderService(){

  private val ioScope = CoroutineScope(Dispatchers.IO)

  override fun onTileRequest(
    requestParams: RequestReaders.TileRequest): ListenableFuture<TileBuilders.Tile> {

    return ioScope.future {

      val deviceParams = requestParams.deviceParameters

      val glassCount = waterRepository.getGlassCount()

      TileBuilders.Tile.builder()
        .setResourcesVersion("1")
        .setTimeline(
          TimelineBuilders.Timeline.builder().addTimelineEntry(
            TimelineBuilders.TimelineEntry.builder().setLayout(
              LayoutElementBuilders.Layout.builder().setRoot(
                LayoutElementBuilders.Box.builder()
                  .setWidth(DimensionBuilders.expand())
                  .setHeight(DimensionBuilders.expand())
                  .addContent(
                    currentGlassText(glassCount.toString(), deviceParams)
                  )
              )
            )
          )
        ).build()

    }
  }

  private fun currentGlassText(current: String, deviceParameters: DeviceParametersReaders.DeviceParameters) = LayoutElementBuilders.Text.builder()
    .setText(current)
    .setFontStyle(LayoutElementBuilders.FontStyles.display2(deviceParameters))
    .build()

  override fun onResourcesRequest(
    requestParams: RequestReaders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {

    return ioScope.future {
      ResourceBuilders.Resources.builder()
        .setVersion("1")
        .build()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    ioScope.cancel()
  }

}
