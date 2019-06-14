package com.raywenderlich.android.creaturemon.allcreatures

import androidx.lifecycle.ViewModel
import com.raywenderlich.android.creaturemon.addcreature.AddCreatureIntent
import com.raywenderlich.android.creaturemon.addcreature.AddCreatureViewModel
import com.raywenderlich.android.creaturemon.addcreature.AddCreatureViewState
import com.raywenderlich.android.creaturemon.mvibase.MviViewModel
import com.raywenderlich.android.creaturemon.util.notOfType
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class AllCreaturesViewModel(
        private val actionProcessorHolder: AllCreaturesProcessorHolder
) : ViewModel(), MviViewModel<AllCreaturesIntent, AllCreaturesViewState> {

    private val intentsSubject: PublishSubject<AllCreaturesIntent> = PublishSubject.create()
    private val statesObservable: Observable<AllCreaturesViewState> = compose()

    private val intentFilter: ObservableTransformer<AllCreaturesIntent, AllCreaturesIntent>
        get() = ObservableTransformer { intents ->
            intents.publish{ shared ->
                Observable.merge(
                        shared.ofType(AllCreaturesIntent.LoadAllCreaturesIntent::class.java).take(1),
                        shared.notOfType(AllCreaturesIntent.LoadAllCreaturesIntent::class.java)
                )
            }
        }

    override fun processIntents(intents: Observable<AllCreaturesIntent>) {
        intents.subscribe(intentsSubject)
    }

    override fun states(): Observable<AllCreaturesViewState> = statesObservable

    private fun compose(): Observable<AllCreaturesViewState> {
        return intentsSubject
                .compose(intentFilter)
                .map(this::actionFromIntent)
                .compose(actionProcessorHolder.actionProcessor)
                .scan(AllCreaturesViewState.idle(), reducer)
                .distinctUntilChanged()
                .replay()
                .autoConnect(0)
    }

    private fun actionFromIntent(intent: AllCreaturesIntent): AllCreaturesAction {
        return when(intent) {
            is AllCreaturesIntent.LoadAllCreaturesIntent -> AllCreaturesAction.LoadAllCreaturesAction
            is AllCreaturesIntent.ClearAllCreaturesIntent -> AllCreaturesAction.ClearAllCreaturesAction
        }
    }

    companion object {
        private val reducer = BiFunction { previousState: AllCreaturesViewState, result: AllCreaturesResult ->
            when (result) {
                is AllCreaturesResult.LoadAllCreaturesResult -> when (result) {
                    is AllCreaturesResult.LoadAllCreaturesResult.Success -> {
                        previousState.copy(isLoading = false, creatures = result.creatures)
                    }
                    is AllCreaturesResult.LoadAllCreaturesResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                    is AllCreaturesResult.LoadAllCreaturesResult.Loading -> previousState.copy(isLoading = true)
                }
                is AllCreaturesResult.ClearAllCreaturesResult -> when (result) {
                    is AllCreaturesResult.ClearAllCreaturesResult.Success -> {
                        previousState.copy(isLoading = false, creatures = emptyList())
                    }
                    is AllCreaturesResult.ClearAllCreaturesResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                    is AllCreaturesResult.ClearAllCreaturesResult.Clearing -> previousState.copy(isLoading = true)
                }
            }
        }
    }
}