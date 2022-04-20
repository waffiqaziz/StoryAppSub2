package com.dicoding.storyapp.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.storyapp.DataDummy
import com.dicoding.storyapp.MainCoroutineRule
import com.dicoding.storyapp.data.remote.response.ListStoryItem
import com.dicoding.storyapp.getOrAwaitValue
import com.dicoding.storyapp.ui.adapter.StoryAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ListStoryViewModelTest {
  @get:Rule
  var instantExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  var mainCoroutineRules = MainCoroutineRule()

  @Mock
  private lateinit var listStoryViewModel: ListStoryViewModel


  @Test
  fun `when Get Quote Should Not Null`() = mainCoroutineRules.runBlockingTest {
    val dummyStory = DataDummy.generateDummyStoriesResponsePaging()
    val data = PagedTestDataSources.snapshot(dummyStory)
    val story = MutableLiveData<PagingData<ListStoryItem>>()
    story.value = data

    Mockito.`when`(listStoryViewModel.getStory("token")).thenReturn(story)
    val actualNews = listStoryViewModel.getStory("token").getOrAwaitValue()

    val differ = AsyncPagingDataDiffer(
      diffCallback = StoryAdapter.DIFF_CALLBACK,
      updateCallback = noopListUpdateCallback,
      mainDispatcher = mainCoroutineRules.dispatcher,
      workerDispatcher = mainCoroutineRules.dispatcher,
    )

    differ.submitData(actualNews)

    advanceUntilIdle()
    Mockito.verify(listStoryViewModel).getStory("token")
    Assert.assertNotNull(differ.snapshot())
    Assert.assertEquals(dummyStory.size, differ.snapshot().size)
    Assert.assertEquals(dummyStory[0].name, differ.snapshot()[0]?.name)
  }
}

class PagedTestDataSources private constructor() :
  PagingSource<Int, LiveData<List<ListStoryItem>>>() {
  companion object {
    fun snapshot(items: List<ListStoryItem>): PagingData<ListStoryItem> {
      return PagingData.from(items)
    }
  }

  override fun getRefreshKey(state: PagingState<Int, LiveData<List<ListStoryItem>>>): Int {
    return 0
  }

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<ListStoryItem>>> {
    return LoadResult.Page(emptyList(), 0, 1)
  }
}

val noopListUpdateCallback = object : ListUpdateCallback {
  override fun onInserted(position: Int, count: Int) {}
  override fun onRemoved(position: Int, count: Int) {}
  override fun onMoved(fromPosition: Int, toPosition: Int) {}
  override fun onChanged(position: Int, count: Int, payload: Any?) {}
}