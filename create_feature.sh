#!/usr/bin/env bash

# 遇到错误立即退出
set -e

# 配色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

if [ -z "$1" ]; then
    echo -e "${RED}[错误] 请提供特性模块名称！${NC}"
    echo -e "用法: ./create_feature.sh <feature_name> [--with-data]"
    echo -e "示例: ./create_feature.sh profile"
    echo -e "      ./create_feature.sh profile --with-data"
    exit 1
fi

RAW_NAME=""
WITH_DATA=false
while [[ $# -gt 0 ]]; do
    case "$1" in
        --with-data)
            WITH_DATA=true
            shift
            ;;
        -*)
            echo -e "${RED}[错误] 未知参数: $1${NC}"
            echo -e "用法: ./create_feature.sh <feature_name> [--with-data]"
            exit 1
            ;;
        *)
            if [ -z "$RAW_NAME" ]; then
                RAW_NAME="$1"
            else
                echo -e "${RED}[错误] 多余的参数: $1${NC}"
                exit 1
            fi
            shift
            ;;
    esac
done

if [ -z "$RAW_NAME" ]; then
    echo -e "${RED}[错误] 请提供特性模块名称！${NC}"
    exit 1
fi

# 统一转换为小写下划线，避免连字符引发 Kotlin 包名/标识符非法
FEATURE_NAME=$(echo "$RAW_NAME" | tr '[:upper:]-' '[:lower:]_')
if ! echo "$FEATURE_NAME" | grep -qE '^[a-z][a-z0-9_]*$'; then
    echo -e "${RED}[错误] 非法的特性模块名称: ${RAW_NAME} (仅支持字母/数字/下划线/连字符且需以字母开头)${NC}"
    exit 1
fi

# 转为驼峰命名作为类名前缀与 Koin 模块前缀 (profile -> Profile / profile, user_profile -> UserProfile / userProfile)
# Kotlin 包名与 Android namespace 去除下划线以符合 ktlint(standard:package-name) 规范
FEATURE_PKG="${FEATURE_NAME//_/}"
CLASS_PREFIX=$(echo "$FEATURE_NAME" | awk -F_ '{for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) substr($i,2)} 1' OFS="")
MODULE_PREFIX=$(echo "$CLASS_PREFIX" | awk '{print tolower(substr($0,1,1)) substr($0,2)}')

echo -e "${BLUE}====================================================${NC}"
echo -e "${BLUE}    Creating Feature: ${FEATURE_NAME} (${CLASS_PREFIX})    ${NC}"
echo -e "${BLUE}====================================================${NC}"

# 获取当前项目的约定插件前缀与包名骨架
PKG_PREFIX="xxx.yyy.zzz"
PLUGIN_PREFIX="myproject"

API_DIR="features/${FEATURE_NAME}/api"
IMPL_DIR="features/${FEATURE_NAME}/impl"

if [ -d "features/${FEATURE_NAME}" ]; then
    echo -e "${RED}[错误] 目录 features/${FEATURE_NAME} 已存在！${NC}"
    exit 1
fi

echo -e "${YELLOW}[1/5] 创建目录结构...${NC}"
mkdir -p "${API_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_PKG}/api"
mkdir -p "${IMPL_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_PKG}/impl"
mkdir -p "${IMPL_DIR}/src/commonTest/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_PKG}/impl"

echo -e "${YELLOW}[2/5] 生成 api 模块...${NC}"
cat <<EOF > "${API_DIR}/build.gradle.kts"
plugins {
    alias(libs.plugins.${PLUGIN_PREFIX}.kmp.feature.api)
}

kotlin {
    android {
        namespace = "${PKG_PREFIX}.feature.${FEATURE_PKG}.api"
    }
}
EOF

cat <<EOF > "${API_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_PKG}/api/${CLASS_PREFIX}NavKey.kt"
package ${PKG_PREFIX}.feature.${FEATURE_PKG}.api

import kotlinx.serialization.Serializable
import ${PKG_PREFIX}.core.navigation.NavKey

/**
 * ${CLASS_PREFIX} 业务模块路由键。
 */
@Serializable
data object ${CLASS_PREFIX}NavKey : NavKey
EOF

echo -e "${YELLOW}[3/5] 生成 impl 模块与单测样板...${NC}"
if [ "$WITH_DATA" = true ]; then
cat <<EOF > "${IMPL_DIR}/build.gradle.kts"
plugins {
    alias(libs.plugins.${PLUGIN_PREFIX}.kmp.feature.impl)
}

kotlin {
    android {
        namespace = "${PKG_PREFIX}.feature.${FEATURE_PKG}.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:${FEATURE_NAME}:api"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
        }
    }
}
EOF

cat <<EOF > "${IMPL_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_PKG}/impl/${CLASS_PREFIX}Repository.kt"
package ${PKG_PREFIX}.feature.${FEATURE_PKG}.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ${PKG_PREFIX}.core.database.DummyDao
import ${PKG_PREFIX}.core.database.DummyEntity
import ${PKG_PREFIX}.core.model.DummyModel
import ${PKG_PREFIX}.core.model.NetworkResult
import ${PKG_PREFIX}.core.network.NetworkDataSource

/**
 * ${CLASS_PREFIX} 离线优先（Offline-First）数据仓库。
 * 优先观察本地 Room 数据流，并按需拉取远端更新写回缓存。
 */
class ${CLASS_PREFIX}Repository(
    private val networkDataSource: NetworkDataSource,
    private val dummyDao: DummyDao,
) {
    fun observeItems(): Flow<List<DummyModel>> =
        dummyDao.getAllDummies().map { entities ->
            entities.map { entity ->
                DummyModel(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                )
            }
        }

    suspend fun refresh(): NetworkResult<List<DummyModel>> {
        val result = networkDataSource.fetchDummyData()
        if (result is NetworkResult.Success) {
            dummyDao.insertDummies(
                result.data.map { model ->
                    DummyEntity(
                        id = model.id,
                        title = model.title,
                        description = model.description,
                    )
                },
            )
        }
        return result
    }
}
EOF

cat <<EOF > "${IMPL_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_PKG}/impl/${CLASS_PREFIX}UiState.kt"
package ${PKG_PREFIX}.feature.${FEATURE_PKG}.impl

import ${PKG_PREFIX}.core.model.DummyModel

/**
 * ${CLASS_PREFIX} UI 界面状态模型。
 */
data class ${CLASS_PREFIX}UiState(
    val title: String = "${CLASS_PREFIX} Feature",
    val items: List<DummyModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
EOF

cat <<EOF > "${IMPL_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_PKG}/impl/${CLASS_PREFIX}ViewModel.kt"
package ${PKG_PREFIX}.feature.${FEATURE_PKG}.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ${PKG_PREFIX}.core.model.NetworkResult

/**
 * ${CLASS_PREFIX} 跨平台 ViewModel 状态机（Offline-First 单向数据流）。
 */
class ${CLASS_PREFIX}ViewModel(
    private val repository: ${CLASS_PREFIX}Repository,
) : ViewModel() {
    private val loadingState = MutableStateFlow(false)
    private val errorState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<${CLASS_PREFIX}UiState> =
        combine(
            repository.observeItems(),
            loadingState,
            errorState,
        ) { items, isLoading, errorMessage ->
            ${CLASS_PREFIX}UiState(
                items = items,
                isLoading = isLoading,
                errorMessage = errorMessage,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ${CLASS_PREFIX}UiState(),
        )

    fun refresh() {
        if (loadingState.value) return
        viewModelScope.launch {
            loadingState.value = true
            errorState.value = null
            when (val result = repository.refresh()) {
                is NetworkResult.Error -> {
                    errorState.value = result.message.ifBlank { "加载失败，请重试" }
                }
                is NetworkResult.Success, NetworkResult.Loading -> Unit
            }
            loadingState.value = false
        }
    }
}
EOF

cat <<EOF > "${IMPL_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_PKG}/impl/${CLASS_PREFIX}Screen.kt"
package ${PKG_PREFIX}.feature.${FEATURE_PKG}.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ${PKG_PREFIX}.core.ui.ErrorView
import ${PKG_PREFIX}.core.ui.LoadingView
import ${PKG_PREFIX}.core.ui.generated.resources.Res
import ${PKG_PREFIX}.core.ui.generated.resources.back

/**
 * ${CLASS_PREFIX} 业务呈现页面。
 */
@Composable
fun ${CLASS_PREFIX}Screen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ${CLASS_PREFIX}ViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = onBack) {
                Text(stringResource(Res.string.back))
            }
            Text(
                text = uiState.title,
                style = MaterialTheme.typography.titleMedium,
            )
            ElevatedButton(onClick = viewModel::refresh) {
                Text("Refresh")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading && uiState.items.isEmpty() -> {
                LoadingView(modifier = Modifier.fillMaxSize())
            }
            uiState.errorMessage != null && uiState.items.isEmpty() -> {
                ErrorView(
                    message = uiState.errorMessage.orEmpty(),
                    onRetry = viewModel::refresh,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        Text(
                            text = "\${item.title} — \${item.description}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
EOF

cat <<EOF > "${IMPL_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_PKG}/impl/${CLASS_PREFIX}Module.kt"
package ${PKG_PREFIX}.feature.${FEATURE_PKG}.impl

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * ${CLASS_PREFIX} 业务模块依赖注入。
 */
val ${MODULE_PREFIX}Module =
    module {
        singleOf(::${CLASS_PREFIX}Repository)
        viewModelOf(::${CLASS_PREFIX}ViewModel)
    }
EOF

cat <<EOF > "${IMPL_DIR}/src/commonTest/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_PKG}/impl/${CLASS_PREFIX}ViewModelTest.kt"
package ${PKG_PREFIX}.feature.${FEATURE_PKG}.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import ${PKG_PREFIX}.core.database.DummyDao
import ${PKG_PREFIX}.core.database.DummyEntity
import ${PKG_PREFIX}.core.model.NetworkResult
import ${PKG_PREFIX}.core.network.FakeNetworkDataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeDummyDao : DummyDao {
    private val store = MutableStateFlow<List<DummyEntity>>(emptyList())

    override fun getAllDummies(): Flow<List<DummyEntity>> = store

    override suspend fun insertDummies(dummies: List<DummyEntity>) {
        store.value = dummies
    }

    override suspend fun clearAll() {
        store.value = emptyList()
    }
}

/**
 * ${CLASS_PREFIX} Offline-First Repository 与 ViewModel 状态样板测试。
 */
class ${CLASS_PREFIX}ViewModelTest {
    @Test
    fun testRepositoryRefreshWritesLocalCache() =
        runTest {
            val fakeDao = FakeDummyDao()
            val fakeNetwork = FakeNetworkDataSource()
            val repository = ${CLASS_PREFIX}Repository(fakeNetwork, fakeDao)

            val result = repository.refresh()
            assertTrue(result is NetworkResult.Success)
            assertEquals(2, repository.observeItems().first().size)
        }
}
EOF
else
cat <<EOF > "${IMPL_DIR}/build.gradle.kts"
plugins {
    alias(libs.plugins.${PLUGIN_PREFIX}.kmp.feature.impl)
}

kotlin {
    android {
        namespace = "${PKG_PREFIX}.feature.${FEATURE_PKG}.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:${FEATURE_NAME}:api"))
        }
    }
}
EOF

cat <<EOF > "${IMPL_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_PKG}/impl/${CLASS_PREFIX}UiState.kt"
package ${PKG_PREFIX}.feature.${FEATURE_PKG}.impl

/**
 * ${CLASS_PREFIX} UI 界面状态模型。
 */
data class ${CLASS_PREFIX}UiState(
    val title: String = "${CLASS_PREFIX} Feature",
    val isLoading: Boolean = false,
)
EOF

cat <<EOF > "${IMPL_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_PKG}/impl/${CLASS_PREFIX}ViewModel.kt"
package ${PKG_PREFIX}.feature.${FEATURE_PKG}.impl

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ${CLASS_PREFIX} 跨平台 ViewModel 状态机。
 */
class ${CLASS_PREFIX}ViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(${CLASS_PREFIX}UiState())
    val uiState: StateFlow<${CLASS_PREFIX}UiState> = _uiState.asStateFlow()
}
EOF

cat <<EOF > "${IMPL_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_PKG}/impl/${CLASS_PREFIX}Screen.kt"
package ${PKG_PREFIX}.feature.${FEATURE_PKG}.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ${PKG_PREFIX}.core.ui.generated.resources.Res
import ${PKG_PREFIX}.core.ui.generated.resources.back

/**
 * ${CLASS_PREFIX} 业务呈现页面。
 */
@Composable
fun ${CLASS_PREFIX}Screen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ${CLASS_PREFIX}ViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = onBack) {
                Text(stringResource(Res.string.back))
            }
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = uiState.title,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }
}
EOF

cat <<EOF > "${IMPL_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_PKG}/impl/${CLASS_PREFIX}Module.kt"
package ${PKG_PREFIX}.feature.${FEATURE_PKG}.impl

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * ${CLASS_PREFIX} 业务模块依赖注入。
 */
val ${MODULE_PREFIX}Module =
    module {
        viewModelOf(::${CLASS_PREFIX}ViewModel)
    }
EOF

cat <<EOF > "${IMPL_DIR}/src/commonTest/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_PKG}/impl/${CLASS_PREFIX}ViewModelTest.kt"
package ${PKG_PREFIX}.feature.${FEATURE_PKG}.impl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * ${CLASS_PREFIX} ViewModel 单元测试样板。
 */
class ${CLASS_PREFIX}ViewModelTest {
    @Test
    fun testInitialState() {
        val viewModel = ${CLASS_PREFIX}ViewModel()
        val state = viewModel.uiState.value

        assertEquals("${CLASS_PREFIX} Feature", state.title)
        assertFalse(state.isLoading)
    }
}
EOF
fi

echo -e "${YELLOW}[4/5] 自动装配模块依赖与全局路由/DI...${NC}"

# 1. 注册 settings.gradle.kts
SETTINGS_FILE="settings.gradle.kts"
if grep -q "// \[FEATURE_MODULES_END\]" "$SETTINGS_FILE"; then
    awk -v text="// [FEATURE:${FEATURE_NAME}:START]\ninclude(\":features:${FEATURE_NAME}:api\")\ninclude(\":features:${FEATURE_NAME}:impl\")\n// [FEATURE:${FEATURE_NAME}:END]" '
        /\/\/ \[FEATURE_MODULES_END\]/ { print text }
        { print }
    ' "$SETTINGS_FILE" > "$SETTINGS_FILE.tmp" && mv "$SETTINGS_FILE.tmp" "$SETTINGS_FILE"
else
    cat <<EOF >> "$SETTINGS_FILE"

// [FEATURE:${FEATURE_NAME}:START]
include(":features:${FEATURE_NAME}:api")
include(":features:${FEATURE_NAME}:impl")
// [FEATURE:${FEATURE_NAME}:END]
EOF
fi

# 2. 注入 shared/build.gradle.kts 依赖
SHARED_BUILD="shared/build.gradle.kts"
if [ -f "$SHARED_BUILD" ] && grep -q "// \[FEATURE_DEPENDENCIES_END\]" "$SHARED_BUILD"; then
    awk -v text="            // [FEATURE:${FEATURE_NAME}:START]\n            implementation(project(\":features:${FEATURE_NAME}:api\"))\n            implementation(project(\":features:${FEATURE_NAME}:impl\"))\n            // [FEATURE:${FEATURE_NAME}:END]" '
        /\/\/ \[FEATURE_DEPENDENCIES_END\]/ { print text }
        { print }
    ' "$SHARED_BUILD" > "$SHARED_BUILD.tmp" && mv "$SHARED_BUILD.tmp" "$SHARED_BUILD"
fi

# 3. 注册 Koin DI (AppBootstrap.kt)
BOOTSTRAP_FILE=$(find shared/src/commonMain/kotlin -name "AppBootstrap.kt" | head -n 1)
if [ -n "$BOOTSTRAP_FILE" ] && [ -f "$BOOTSTRAP_FILE" ]; then
    if grep -q "// \[FEATURE_IMPORTS_END\]" "$BOOTSTRAP_FILE"; then
        awk -v text="// [FEATURE:${FEATURE_NAME}:START]\nimport ${PKG_PREFIX}.feature.${FEATURE_PKG}.impl.${MODULE_PREFIX}Module\n// [FEATURE:${FEATURE_NAME}:END]" '
            /\/\/ \[FEATURE_IMPORTS_END\]/ { print text }
            { print }
        ' "$BOOTSTRAP_FILE" > "$BOOTSTRAP_FILE.tmp" && mv "$BOOTSTRAP_FILE.tmp" "$BOOTSTRAP_FILE"
    fi
    if grep -q "// \[FEATURE_KOIN_MODULES_END\]" "$BOOTSTRAP_FILE"; then
        awk -v text="                    // [FEATURE:${FEATURE_NAME}:START]\n                    ${MODULE_PREFIX}Module,\n                    // [FEATURE:${FEATURE_NAME}:END]" '
            /\/\/ \[FEATURE_KOIN_MODULES_END\]/ { print text }
            { print }
        ' "$BOOTSTRAP_FILE" > "$BOOTSTRAP_FILE.tmp" && mv "$BOOTSTRAP_FILE.tmp" "$BOOTSTRAP_FILE"
    fi
fi

# 4. 挂载路由 (AppNavGraph.kt)
NAVGRAPH_FILE=$(find shared/src/commonMain/kotlin -name "AppNavGraph.kt" | head -n 1)
if [ -n "$NAVGRAPH_FILE" ] && [ -f "$NAVGRAPH_FILE" ]; then
    if grep -q "// \[FEATURE_IMPORTS_END\]" "$NAVGRAPH_FILE"; then
        awk -v text="// [FEATURE:${FEATURE_NAME}:START]\nimport ${PKG_PREFIX}.feature.${FEATURE_PKG}.api.${CLASS_PREFIX}NavKey\nimport ${PKG_PREFIX}.feature.${FEATURE_PKG}.impl.${CLASS_PREFIX}Screen\n// [FEATURE:${FEATURE_NAME}:END]" '
            /\/\/ \[FEATURE_IMPORTS_END\]/ { print text }
            { print }
        ' "$NAVGRAPH_FILE" > "$NAVGRAPH_FILE.tmp" && mv "$NAVGRAPH_FILE.tmp" "$NAVGRAPH_FILE"
    fi
    if grep -q "// \[FEATURE_NAV_CASES_END\]" "$NAVGRAPH_FILE"; then
        awk -v text="        // [FEATURE:${FEATURE_NAME}:START]\n        is ${CLASS_PREFIX}NavKey -> {\n            ${CLASS_PREFIX}Screen(\n                onBack = { navigator.goBack() },\n                modifier = modifier,\n            )\n        }\n        // [FEATURE:${FEATURE_NAME}:END]" '
            /\/\/ \[FEATURE_NAV_CASES_END\]/ { print text }
            { print }
        ' "$NAVGRAPH_FILE" > "$NAVGRAPH_FILE.tmp" && mv "$NAVGRAPH_FILE.tmp" "$NAVGRAPH_FILE"
    fi
fi

echo -e "${YELLOW}[5/5] 执行 Spotless 格式化...${NC}"
chmod +x gradlew
./gradlew spotlessApply > /dev/null 2>&1 || true

echo -e "\n${GREEN}====================================================${NC}"
echo -e "${GREEN}🎉 特性模块 ${FEATURE_NAME} 创建并自动挂载完成！${NC}"
echo -e "模块路径："
echo -e "  - API:  :features:${FEATURE_NAME}:api"
echo -e "  - IMPL: :features:${FEATURE_NAME}:impl"
echo -e "已自动挂载到："
echo -e "  - settings.gradle.kts"
echo -e "  - shared/build.gradle.kts"
echo -e "  - AppBootstrap.kt (Koin Module)"
echo -e "  - AppNavGraph.kt (Navigation Route)"
echo -e "====================================================${NC}"
