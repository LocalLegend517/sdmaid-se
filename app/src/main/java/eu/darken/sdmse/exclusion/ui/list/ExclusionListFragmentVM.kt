package eu.darken.sdmse.exclusion.ui.list

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.sdmse.common.coroutine.DispatcherProvider
import eu.darken.sdmse.common.debug.logging.logTag
import eu.darken.sdmse.common.pkgs.PkgRepo
import eu.darken.sdmse.common.pkgs.getPkg
import eu.darken.sdmse.common.uix.ViewModel3
import eu.darken.sdmse.exclusion.core.ExclusionManager
import eu.darken.sdmse.exclusion.core.types.PathExclusion
import eu.darken.sdmse.exclusion.core.types.PkgExclusion
import eu.darken.sdmse.exclusion.core.types.SegmentExclusion
import eu.darken.sdmse.exclusion.ui.list.types.PackageExclusionVH
import eu.darken.sdmse.exclusion.ui.list.types.PathExclusionVH
import eu.darken.sdmse.exclusion.ui.list.types.SegmentExclusionVH
import eu.darken.sdmse.main.ui.dashboard.items.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ExclusionListFragmentVM @Inject constructor(
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    @ApplicationContext private val context: Context,
    private val exclusionManager: ExclusionManager,
    private val pkgRepo: PkgRepo,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    val state = exclusionManager.exclusions
        .map { exclusions ->
            val items = exclusions.map { exclusion ->
                when (exclusion) {
                    is PkgExclusion -> PackageExclusionVH.Item(
                        pkg = pkgRepo.getPkg(exclusion.pkgId).firstOrNull(),
                        exclusion = exclusion,
                        onItemClick = {
                            ExclusionListFragmentDirections.actionExclusionsListFragmentToPkgExclusionFragment(
                                exclusionId = exclusion.id,
                                initial = null
                            ).navigate()
                        }
                    )

                    is PathExclusion -> PathExclusionVH.Item(
                        exclusion = exclusion,
                        onItemClick = {
                            ExclusionListFragmentDirections.actionExclusionsListFragmentToPathExclusionFragment(
                                exclusionId = exclusion.id,
                                initial = null
                            ).navigate()
                        }
                    )

                    is SegmentExclusion -> SegmentExclusionVH.Item(
                        exclusion = exclusion,
                        onItemClick = {
                            ExclusionListFragmentDirections.actionExclusionsListFragmentToSegmentExclusionFragment(
                                exclusionId = exclusion.id,
                                initial = null
                            ).navigate()
                        }
                    )

                    else -> throw NotImplementedError()
                }
            }
            val sortedItems = items.sortedWith(
                compareBy<ExclusionListAdapter.Item> {
                    when (it) {
                        is PackageExclusionVH.Item -> 0
                        is PathExclusionVH.Item -> 1
                        is SegmentExclusionVH.Item -> 2
                        else -> -1
                    }
                }.thenBy {
                    it.exclusion.label.get(context)
                }
            )
            State(sortedItems, loading = false)
        }
        .onStart { emit(State()) }
        .asLiveData2()

    data class State(
        val items: List<ExclusionListAdapter.Item> = emptyList(),
        val loading: Boolean = true,
    )

    companion object {
        private val TAG = logTag("Exclusions", "List", "Fragment", "VM")
    }
}