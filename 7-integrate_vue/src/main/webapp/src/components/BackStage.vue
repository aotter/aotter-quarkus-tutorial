<template>
  <div class="container" style="margin-top: 150px">
    <div class="py-5 bg-light mt-5">

      <div class="title d-flex">文章列表</div>
      <button class="btn btn-primary mt-2 mb-3 add d-flex">
        <router-link to="/article-edit" style="color: whitesmoke; text-decoration: none">新增文章</router-link>
      </button>

<!--    <div class="mt-3 mb-3 d-flex">-->
<!--      <input class="form-control mr-sm-2 w-30" type="search" placeholder="文章名稱" aria-label="tag">-->
<!--      <select class="form-control mr-sm-4 w-30">-->
<!--        <option value="">請選擇</option>-->
<!--        <option value="分類ㄧ">分類ㄧ</option>-->
<!--        <option value="分類二">分類二</option>-->
<!--        <option value="分類三">分類三</option>-->
<!--      </select>-->
<!--      <button class="btn btn-outline-success my-2 my-sm-0 w-30">搜尋</button>-->
<!--    </div>-->

      <table class="table table-bordered text-center">
      <thead class="table-secondary" >
      <tr>
        <th scope="col" width="40%">文章名稱</th>
        <th scope="col" width="20%">文章類別</th>
        <th scope="col" width="20%">最後更新日期</th>
        <th scope="col" width="20%"></th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(article,index) in articleList" :key="index">
        <th scope="row">
          <a :href="'/article-content?articleId='+article.id">
            {{article.title}}
          </a>
        </th>
        <td>{{article.category}}</td>
        <td>{{article.lastModifiedTime}}</td>
        <td class="d-flex flex-wrap">
        <span class="my-1">
          <button class="btn btn-warning mr-2">
            <router-link :to="'/article-edit?articleId='+article.id" style="color: whitesmoke; text-decoration: none">編輯</router-link>
          </button>
          <button class="btn btn-danger" @click="deleteArticle(article.id,index)">刪除</button>
        </span>
        </td>
      </tr>
      </tbody>
    </table>

      <nav aria-label="Page navigation example">
      <ul class="pagination justify-content-center">
        <li class="page-item" :class="{'disabled':currentPage == 1}" >
          <a class="page-link"  @click="previousPage()" aria-label="Previous" :disabled="currentPage == 1">
            <span aria-hidden="true">&laquo;</span>
            <span class="sr-only">Previous</span>
          </a>
        </li>
        <li class="page-item" v-for="page in pageLength" :key="page">
          <a class="page-link" href="#" @click="changePage(page)">{{page}}</a>
        </li>
        <li class="page-item" :class="{'disabled':currentPage == pageLength}">
          <a class="page-link" @click="nextPage(pageLength)" aria-label="Next" :disabled="currentPage == pageLength">
            <span aria-hidden="true">&raquo;</span>
            <span class="sr-only">Next</span>
          </a>
        </li>
      </ul>
    </nav>
    </div>
  </div>
</template>

<script>
export default {
  name: "BackStage",
  data () {
    return {
      currentPage: 1,
      // pageLength: 1,
      param: {
        name: '',
        type: '',
        tag: ''
      },
      articleList:[],
    }
  },
  computed:{
    pageLength(){
      return this.articleList.length != 0 ? parseInt( this.articleList[0].pageLength) : 1
    }
  },
  created() {
    this.getArticleList(1)
  },
  methods:{
    getArticleList:function (page){
      this.articleList = []
      let url = `/articles?page=${page}`
      fetch(url)
          .then(res => {
            if (res.ok) {
              return res.json()
            }
          })
          .then(list => {
            console.log("list=",list)
            this.articleList = list
          })
    },
    deleteArticle: function(articleId,index){
      let url = `/article?articleId=${articleId}`
      fetch(url, {
        method: 'DELETE',
        headers: {
          Accept: 'application/json', 'Content-Type': 'application/json'
        }
      }).then(res => {
            if (res.ok) {
              return res.json()
            }
          })
          .then(result => {
            console.log("result=",result)
            // this.pageLength = list.length != 0 ? parseInt(list[0].pageLength) : 1
            this.articleList.splice(index,1)
          })
    },
    // query:function(){
    //   let url = Const.bgDomain + '/material-query?subject=' + this.$route.query.subject
    //   url += `&backendName=${this.param.name}`
    //   url += `&questionType=${this.param.type}`
    //   url += `&tag=${this.param.tag}`
    //   fetch(url)
    //       .then(res => {
    //         if (res.ok) {
    //           return res.json()
    //         }
    //       })
    //       .then(list => {
    //         this.pageLength = list.length != 0 ? parseInt(list[0].pageLength) : 1
    //         this.materialList = list
    //       })
    // },
    changePage: function (page){
      this.currentPage = page
      this.getArticleList(page)
    },
    previousPage: function (){
      if(this.currentPage !== 1){
        this.currentPage = this.currentPage-1
        this.getArticleList(this.currentPage)
      }
    },
    nextPage: function (pageLength){
      if(this.currentPage !== pageLength) {
        this.currentPage = this.currentPage + 1
        this.getArticleList(this.currentPage)
      }
    }
  }
}



</script>

<style scoped>
.title{
  font-size: 24px;
}
.add{
  color: white;
}
.page-item{
  cursor: pointer;
}
</style>