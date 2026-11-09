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
    echo -e "用法: ./create_feature.sh <feature_name>"
    echo -e "示例: ./create_feature.sh profile"
    exit 1
fi

RAW_NAME="$1"
# 转换为小写
FEATURE_NAME=$(echo "$RAW_NAME" | tr '[:upper:]' '[:lower:]')
# 转为驼峰命名作为类名前缀 (跨平台支持 macOS 与 Linux, 例如 profile -> Profile, user_profile -> UserProfile)
CLASS_PREFIX=$(echo "$FEATURE_NAME" | awk -F_ '{for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) substr($i,2)} 1' OFS="")

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
mkdir -p "${API_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_NAME}/api"
mkdir -p "${IMPL_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_NAME}/impl"
mkdir -p "${IMPL_DIR}/src/commonTest/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_NAME}/impl"

echo -e "${YELLOW}[2/5] 生成 api 模块...${NC}"
cat <<EOF > "${API_DIR}/build.gradle.kts"
plugins {
    alias(libs.plugins.${PLUGIN_PREFIX}.kmp.feature.api)
}

android {
    namespace = "${PKG_PREFIX}.feature.${FEATURE_NAME}.api"
}
EOF

cat <<EOF > "${API_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_NAME}/api/${CLASS_PREFIX}NavKey.kt"
package ${PKG_PREFIX}.feature.${FEATURE_NAME}.api

import kotlinx.serialization.Serializable
import ${PKG_PREFIX}.core.navigation.NavKey

/**
 * ${CLASS_PREFIX} 业务模块路由键。
 */
@Serializable
data object ${CLASS_PREFIX}NavKey : NavKey
EOF

echo -e "${YELLOW}[3/5] 生成 impl 模块与单测样板...${NC}"
cat <<EOF > "${IMPL_DIR}/build.gradle.kts"
plugins {
    alias(libs.plugins.${PLUGIN_PREFIX}.kmp.feature.impl)
}

android {
    namespace = "${PKG_PREFIX}.feature.${FEATURE_NAME}.impl"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:${FEATURE_NAME}:api"))
        }
    }
}
EOF

cat <<EOF > "${IMPL_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_NAME}/impl/${CLASS_PREFIX}UiState.kt"
package ${PKG_PREFIX}.feature.${FEATURE_NAME}.impl

/**
 * ${CLASS_PREFIX} UI 界面状态模型。
 */
data class ${CLASS_PREFIX}UiState(
    val title: String = "${CLASS_PREFIX} Feature",
    val isLoading: Boolean = false,
)
EOF

cat <<EOF > "${IMPL_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_NAME}/impl/${CLASS_PREFIX}ViewModel.kt"
package ${PKG_PREFIX}.feature.${FEATURE_NAME}.impl

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

cat <<EOF > "${IMPL_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_NAME}/impl/${CLASS_PREFIX}Screen.kt"
package ${PKG_PREFIX}.feature.${FEATURE_NAME}.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/**
 * ${CLASS_PREFIX} 业务呈现页面。
 */
@Composable
fun ${CLASS_PREFIX}Screen(
    viewModel: ${CLASS_PREFIX}ViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = uiState.title,
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}
EOF

cat <<EOF > "${IMPL_DIR}/src/commonMain/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_NAME}/impl/${CLASS_PREFIX}Module.kt"
package ${PKG_PREFIX}.feature.${FEATURE_NAME}.impl

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * ${CLASS_PREFIX} 业务模块依赖注入。
 */
val ${FEATURE_NAME}Module =
    module {
        viewModelOf(::${CLASS_PREFIX}ViewModel)
    }
EOF

cat <<EOF > "${IMPL_DIR}/src/commonTest/kotlin/${PKG_PREFIX//.//}/feature/${FEATURE_NAME}/impl/${CLASS_PREFIX}ViewModelTest.kt"
package ${PKG_PREFIX}.feature.${FEATURE_NAME}.impl

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

echo -e "${YELLOW}[4/5] 自动装配模块依赖与全局路由/DI...${NC}"

# 1. 注册 settings.gradle.kts
SETTINGS_FILE="settings.gradle.kts"
if grep -q "// \[FEATURE_MODULES_END\]" "$SETTINGS_FILE"; then
    awk -v text="include(\":features:${FEATURE_NAME}:api\")\ninclude(\":features:${FEATURE_NAME}:impl\")" '
        /\/\/ \[FEATURE_MODULES_END\]/ { print text }
        { print }
    ' "$SETTINGS_FILE" > "$SETTINGS_FILE.tmp" && mv "$SETTINGS_FILE.tmp" "$SETTINGS_FILE"
else
    cat <<EOF >> "$SETTINGS_FILE"

// Feature: ${FEATURE_NAME}
include(":features:${FEATURE_NAME}:api")
include(":features:${FEATURE_NAME}:impl")
EOF
fi

# 2. 注入 shared/build.gradle.kts 依赖
SHARED_BUILD="shared/build.gradle.kts"
if [ -f "$SHARED_BUILD" ] && grep -q "// \[FEATURE_DEPENDENCIES_END\]" "$SHARED_BUILD"; then
    awk -v text="            implementation(project(\":features:${FEATURE_NAME}:api\"))\n            implementation(project(\":features:${FEATURE_NAME}:impl\"))" '
        /\/\/ \[FEATURE_DEPENDENCIES_END\]/ { print text }
        { print }
    ' "$SHARED_BUILD" > "$SHARED_BUILD.tmp" && mv "$SHARED_BUILD.tmp" "$SHARED_BUILD"
fi

# 3. 注册 Koin DI (AppBootstrap.kt)
BOOTSTRAP_FILE=$(find shared/src/commonMain/kotlin -name "AppBootstrap.kt" | head -n 1)
if [ -n "$BOOTSTRAP_FILE" ] && [ -f "$BOOTSTRAP_FILE" ]; then
    if grep -q "// \[FEATURE_IMPORTS_END\]" "$BOOTSTRAP_FILE"; then
        awk -v text="import ${PKG_PREFIX}.feature.${FEATURE_NAME}.impl.${FEATURE_NAME}Module" '
            /\/\/ \[FEATURE_IMPORTS_END\]/ { print text }
            { print }
        ' "$BOOTSTRAP_FILE" > "$BOOTSTRAP_FILE.tmp" && mv "$BOOTSTRAP_FILE.tmp" "$BOOTSTRAP_FILE"
    fi
    if grep -q "// \[FEATURE_KOIN_MODULES_END\]" "$BOOTSTRAP_FILE"; then
        awk -v text="                    ${FEATURE_NAME}Module," '
            /\/\/ \[FEATURE_KOIN_MODULES_END\]/ { print text }
            { print }
        ' "$BOOTSTRAP_FILE" > "$BOOTSTRAP_FILE.tmp" && mv "$BOOTSTRAP_FILE.tmp" "$BOOTSTRAP_FILE"
    fi
fi

# 4. 挂载路由 (AppNavGraph.kt)
NAVGRAPH_FILE=$(find shared/src/commonMain/kotlin -name "AppNavGraph.kt" | head -n 1)
if [ -n "$NAVGRAPH_FILE" ] && [ -f "$NAVGRAPH_FILE" ]; then
    if grep -q "// \[FEATURE_IMPORTS_END\]" "$NAVGRAPH_FILE"; then
        awk -v text="import ${PKG_PREFIX}.feature.${FEATURE_NAME}.api.${CLASS_PREFIX}NavKey\nimport ${PKG_PREFIX}.feature.${FEATURE_NAME}.impl.${CLASS_PREFIX}Screen" '
            /\/\/ \[FEATURE_IMPORTS_END\]/ { print text }
            { print }
        ' "$NAVGRAPH_FILE" > "$NAVGRAPH_FILE.tmp" && mv "$NAVGRAPH_FILE.tmp" "$NAVGRAPH_FILE"
    fi
    if grep -q "// \[FEATURE_NAV_CASES_END\]" "$NAVGRAPH_FILE"; then
        awk -v text="        is ${CLASS_PREFIX}NavKey -> {\n            ${CLASS_PREFIX}Screen(\n                modifier = modifier,\n            )\n        }" '
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
