<template>
  <main role="main" class="container">
    <h1 class="my-5 text-center">{{ $route.meta.title }}</h1>
    <b-button 
      class="mb-2" 
      variant="warning"
      :to="`/posts`"
    >新增文章</b-button>

    <b-table 
      :items="items" 
      :fields="fields" 
      responsive="lg"
    >
      <template #cell(category)="data">
        <b-link :to="`/?category=${data.item.category}&show=${show}`">{{data.item.category}}</b-link>
      </template>

      <template #cell(action)="data">
        <b-button 
          variant="primary" 
          :to="`/posts/${data.item.id}`"
        >編輯</b-button>

        <b-button 
          variant="info"
          @click="publishPost(data.item.id, !data.item.published)"
        >{{data.item.published ? "下架" : "發布"}}
        </b-button>
        
        <b-button 
          @click="deletePost(data.item.id)"
          variant="danger"
        >刪除</b-button>
      </template>
    </b-table>
    <div>
      <b-pagination
        align="center"
        prev-text="« 前一頁"
        next-text="後一頁 »"
        v-model="page"
        :total-rows="total"
        :per-page="show"
        first-number
        last-number
        @change="changePage"
      ></b-pagination>
    </div>
  </main>
</template>

<script>
import { BTable, BButton, BPagination, BLink } from "bootstrap-vue";
import { fetchPostSummary, publishPost, deletePost } from "@/api/post-manage";

export default {
  name: 'HomeView',
  components: {
    BTable,
    BButton,
    BPagination,
    BLink
  },
  data(){
    return {
      fields:[
        {
          key: "title",
          label: "標題",
        },
        {
          key: "category",
          label: "分類",
        },
        {
          key: "authorName",
          label: "作者名稱",
        },
        {
          key: "lastModifiedTime",
          label: "更新時間",
        },
        {
          key: "action",
          label: "操作",
        }
      ],
      items:[],
      page: 1,
      show: 6,
      total: 0,
      authorName: null,
      category: null
    }
  },
  watch:{
    '$route': {
      handler: function(item){
        const query = item.query
   
        if(query.authorName !== undefined){
          this.authorName = query.authorName
        }else{
          this.authorName = null
        }

        if(query.category !== undefined){
          this.category = query.category
        }else{
          this.category = null
        }

        if(query.page !== undefined){
          this.page = query.page
        }else{
          this.page = 1
        }

        if(query.show !== undefined){
          this.show = query.show
        }else{
          this.show = 6
        }

        this.loadData()
      },
      deep: true,
      immediate: true
    }
  },
  methods:{
    loadData: function(){
      fetchPostSummary(this.authorName, this.category, this.page, this.show).then(res => {
        this.items = res.data.list;
        this.total = res.data.total;
      })
    },
    changePage: function(page){
      const query = Object.assign({}, this.$route.query)
      query.page = page
      this.$router.push({query: query})
    },
    publishPost: function(id, status){
      publishPost(id, status).then(res => {
        alert(res.message)
        this.loadData()
      })
    },
    deletePost: function(id){
      deletePost(id).then(res => {
        alert(res.message)
        this.loadData()
      })
    }
  }
}
</script>
