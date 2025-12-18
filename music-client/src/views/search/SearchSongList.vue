<template>
  <div class="search-song-list">
    <play-list :playList="playList" path="song-sheet-detail"></play-list>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, watch, onMounted, getCurrentInstance } from "vue";
import { useStore } from "vuex";
import PlayList from "@/components/PlayList.vue";
import { HttpManager } from "@/api";

import { useRoute } from 'vue-router'; // 修复：使用useRoute获取路由参数
export default defineComponent({
  components: {
    PlayList,
  },
  setup() {
    const { proxy } = getCurrentInstance();
    const store = useStore();

    const route = useRoute();

    const playList = ref([]);
    const searchWord = computed(() => store.getters.searchWord);
    const userId = computed(() => store.getters.userId);
    watch(searchWord, (value) => {
      getSearchList(value);
    });

    async function getSearchList(value) {
      if (!value) return;
      // 获取用户ID（如果已登录）
      const currentUserId = userId.value ? parseInt(userId.value) : null;
      const result = (await HttpManager.getSongListOfLikeTitle(value, currentUserId)) as ResponseBody;
      if (!result.data.length) {
        (proxy as any).$message({
          message: "暂无该歌曲内容",
          type: "warning",
        });
      } else {
        playList.value = result.data;
      }
    }

    onMounted(() => {
      const keywords = route.query.keywords as string;
      if (keywords) {
        getSearchList(keywords);
      }
    });

    return {
      playList,
    };
  },
});
</script>
