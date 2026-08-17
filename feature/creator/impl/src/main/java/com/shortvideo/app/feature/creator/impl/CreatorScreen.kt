package com.shortvideo.app.feature.creator.impl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

/**
 * 创作中心统一页面 Composable（承载拍摄录制与发布编辑两阶段）。
 *
 * @param onClose 点击关闭回调
 * @param onPublishSuccess 发布成功回调
 * @param viewModel 创作 ViewModel
 */
@Composable
fun CreatorScreen(
    onClose: () -> Unit,
    onPublishSuccess: () -> Unit,
    viewModel: CreatorViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.publishSuccess) {
        if (uiState.publishSuccess) {
            onPublishSuccess()
        }
    }

    when (uiState.step) {
        CreatorStep.RECORDING -> {
            RecordingScreen(
                uiState = uiState,
                onToggleRecord = viewModel::toggleRecord,
                onSpeedSelect = viewModel::onSpeedSelected,
                onFlipCamera = viewModel::onFlipCamera,
                onToggleFlash = viewModel::onToggleFlash,
                onNextStep = viewModel::onProceedToPublish,
                onClose = onClose,
                modifier = modifier.fillMaxSize(),
            )
        }

        CreatorStep.PUBLISHING -> {
            PublishScreen(
                uiState = uiState,
                onTitleChange = viewModel::onTitleChange,
                onTagToggle = viewModel::onTagToggle,
                onTogglePublic = viewModel::onTogglePublic,
                onPublish = viewModel::onPublish,
                onBack = viewModel::onBackToRecord,
                modifier = modifier.fillMaxSize(),
            )
        }
    }
}
