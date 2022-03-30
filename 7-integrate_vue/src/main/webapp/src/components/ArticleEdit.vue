<template>
  <div class="container">
    <div class="bg-light py-3">
    <div class="order-md-1">
      <h4 class="mb-3">編輯文章</h4>
      <div id="valid-check" class="needs-validation">
        <label for="title">標題</label>
        <input type="text" class="form-control" id="title" v-model="title" placeholder="" required="">
        <div class="invalid-feedback">
          請輸入文章標題
        </div>

        <div class="mb-3 mt-2">
          <label for="category">文章分類</label>
          <select class="custom-select d-block w-100" id="category" v-model="category" required="">
            <option value="">請選擇...</option>
            <option value="分類一">分類一</option>
            <option value="分類二">分類二</option>
            <option value="分類三">分類三</option>
          </select>
          <div class="invalid-feedback">
            請選擇文章分類
          </div>
        </div>

        <div class="mb-3 mt-2">
          <label for="content">內容</label>
          <textarea v-model="content"  id="content" class="w-100" style="height: 300px" required=""></textarea>
          <div class="invalid-feedback">
            請輸入文章內容
          </div>
        </div>

        <hr class="mb-4">
        <button v-if="articleId" class="btn btn-primary btn-lg btn-block" @click="updateArticle" >更新文章</button>
        <button v-else class="btn btn-primary btn-lg btn-block" @click="postArticle" >儲存文章</button>
      </div>
    </div>
  </div>
  </div>
</template>

<script>
export default {
  name: "ArticleEdit",
  data(){
    return {
      article:{},
      title: '',
      category: '',
      content: '',
    }
  },
  computed:{
    articleId: function (){
      return this.$route.query.articleId
    }
  },
  mounted() {
    if(typeof this.articleId !== 'undefined'){
      let url = `/api/admin/article?articleId=${this.articleId}`
      fetch(url)
          .then(res => {
            if (res.ok) {
              return res.json()
            }
          })
          .then(result => {
            this.title = result.title
            this.category = result.category
            this.content = result.content
          })
    }
  },
  methods: {
    postArticle(){
      if(this.checkContent()) {
        fetch('/api/admin/article', {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify(this.article)
        }).then(res => {
          if (res.ok) {
            this.$router.push(`/back-stage`)
          }
        })
      }
    },
    updateArticle(){
      if(this.checkContent()){
        fetch(`/api/admin/article?articleId=${this.articleId}`,{
          method: 'PUT',
          headers: { 'Content-Type':'application/json' },
          body: JSON.stringify(this.article)
        }).then(res => {
          if (res.ok) {
            this.$router.push(`/back-stage`)
          }
        })
      }
    },
    checkContent(){
      this.article = {}
      let title = this.title.trim()
      let category = this.category.trim()
      let content = this.content.trim()
      if(title.length === 0 || category.length === 0 || content.length === 0){
        document.getElementById('valid-check').classList.add('was-validated')
        return false
      }else {
        this.article = {
          title: title,
          category: category,
          content: content
        }
        return true
      }
    },
  }
}
</script>

<style scoped>

</style>