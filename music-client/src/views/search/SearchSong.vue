<template>
  <div class="search-song">
    <song-list :songList="currentSongList"></song-list>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, watch, onMounted, getCurrentInstance } from "vue";
import { useStore } from "vuex";
import SongList from "@/components/SongList.vue";
import { HttpManager } from "@/api";

import { useRoute } from 'vue-router'; // 修复：使用useRoute获取路由参数


export default defineComponent({
  components: {
    SongList,
  },
  setup() {
    const { proxy } = getCurrentInstance();
    const store = useStore();

    const route = useRoute();

    const currentSongList = ref([]); // 存放的音乐
    const searchWord = computed(() => store.getters.searchWord);
    const userId = computed(() => store.getters.userId);
    watch(searchWord, (value) => {
      searchSong(value);
    });

    // 搜索音乐
    async function searchSong(value) {
      if (!value) {
        currentSongList.value = [];
        return;
      }
      // 获取用户ID（如果已登录）
      const currentUserId = userId.value ? parseInt(userId.value) : null;
      const result = (await HttpManager.getSongOfSingerName(value, currentUserId)) as ResponseBody;
      if (!result.data.length) {
        currentSongList.value = [];
        (proxy as any).$message({
          message: "暂时没有相关歌曲",
          type: "warning",
        });
      } else {
        currentSongList.value = result.data;
      }
    }

    onMounted(() => {
      const keywords = route.query.keywords as string;
      if (keywords) {
        searchSong(keywords);
      }
    });

    return {
      currentSongList,
    };
  },
});
</script>
