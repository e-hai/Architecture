@file:Suppress("ktlint:standard:import-ordering")

package xxx.yyy.zzz.feature.debug.impl

import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import xxx.yyy.zzz.feature.debug.api.DebugSection
import xxx.yyy.zzz.feature.debug.impl.sections.InfraDebugSection
// [KIT_SECTIONS_IMPORTS_START]
// [KIT:abtesting:START]
import xxx.yyy.zzz.feature.debug.impl.sections.AbTestingDebugSection
// [KIT:abtesting:END]
// [KIT:ads:START]
import xxx.yyy.zzz.feature.debug.impl.sections.AdsDebugSection
// [KIT:ads:END]
// [KIT:analytics:START]
import xxx.yyy.zzz.feature.debug.impl.sections.AnalyticsDebugSection
// [KIT:analytics:END]
// [KIT:crashreport:START]
import xxx.yyy.zzz.feature.debug.impl.sections.CrashReportDebugSection
// [KIT:crashreport:END]
// [KIT:mmp:START]
import xxx.yyy.zzz.feature.debug.impl.sections.MmpDebugSection
// [KIT:mmp:END]
// [KIT:pag:START]
import xxx.yyy.zzz.feature.debug.impl.sections.PagDebugSection
// [KIT:pag:END]
// [KIT:pay:START]
import xxx.yyy.zzz.feature.debug.impl.sections.PayDebugSection
// [KIT:pay:END]
// [KIT:push:START]
import xxx.yyy.zzz.feature.debug.impl.sections.PushDebugSection
// [KIT:push:END]
// [KIT:video:START]
import xxx.yyy.zzz.feature.debug.impl.sections.VideoDebugSection
// [KIT:video:END]
// [KIT_SECTIONS_IMPORTS_END]

/**
 * Debug 特性模块依赖注入配置。
 * 通过 `bind<DebugSection>()` 将各 Kit 与基础设施调试面板插件化注册至 Koin。
 */
val debugModule =
    module {
        single(named("infra")) { InfraDebugSection() } bind DebugSection::class

        // [KIT_SECTIONS_REGISTER_START]
        // [KIT:pag:START]
        single(named("pag")) { PagDebugSection() } bind DebugSection::class
        // [KIT:pag:END]
        // [KIT:crashreport:START]
        single(named("crashreport")) { CrashReportDebugSection() } bind DebugSection::class
        // [KIT:crashreport:END]
        // [KIT:analytics:START]
        single(named("analytics")) { AnalyticsDebugSection() } bind DebugSection::class
        // [KIT:analytics:END]
        // [KIT:mmp:START]
        single(named("mmp")) { MmpDebugSection() } bind DebugSection::class
        // [KIT:mmp:END]
        // [KIT:abtesting:START]
        single(named("abtesting")) { AbTestingDebugSection() } bind DebugSection::class
        // [KIT:abtesting:END]
        // [KIT:ads:START]
        single(named("ads")) { AdsDebugSection() } bind DebugSection::class
        // [KIT:ads:END]
        // [KIT:pay:START]
        single(named("pay")) { PayDebugSection() } bind DebugSection::class
        // [KIT:pay:END]
        // [KIT:push:START]
        single(named("push")) { PushDebugSection() } bind DebugSection::class
        // [KIT:push:END]
        // [KIT:video:START]
        single(named("video")) { VideoDebugSection() } bind DebugSection::class
        // [KIT:video:END]
        // [KIT_SECTIONS_REGISTER_END]
    }
