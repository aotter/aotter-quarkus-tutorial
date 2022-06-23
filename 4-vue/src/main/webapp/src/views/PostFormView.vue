<template>
  <main role="main" class="container">
    <h1 class="my-5 text-center">{{ $route.meta.title }}</h1>
    <b-form @submit="submit">
      <b-form-group label="分類" label-for="category">
        <b-form-select
          id="category"
          v-model="form.category"
          :options="category"
          :state="validation.category.state"
        ></b-form-select>
        <b-form-invalid-feedback :state="validation.category.state">
          {{validation.category.message}}
        </b-form-invalid-feedback>
      </b-form-group>

      <b-form-group label="標題" label-for="title">
        <b-form-input
          id="title"
          v-model="form.title"
          type="text"
          :state="validation.title.state"
        ></b-form-input>
        <b-form-invalid-feedback :state="validation.title.state">
          {{validation.title.message}}
        </b-form-invalid-feedback>
      </b-form-group>

      <b-form-group label="內容" label-for="content">
        <b-form-textarea
          id="content"
          v-model="form.content"
          rows="3"
          :state="validation.content.state"
        ></b-form-textarea>
        <b-form-invalid-feedback :state="validation.content.state">
          {{validation.content.message}}
        </b-form-invalid-feedback>
      </b-form-group>

      <p class="text-center">
        <b-button to="/" class="mx-1" type="button" variant="danger">取消</b-button>
        <b-button class="mx-1" type="submit" variant="primary">送出</b-button>
      </p>
    </b-form>
  </main>
</template>
<script>
import {
  BForm,
  BFormGroup,
  BFormInput,
  BFormSelect,
  BFormTextarea,
  BButton,
  BFormInvalidFeedback
} from 'bootstrap-vue';
import {
  fetchPostDetail,
  createPost,
  updatePost
} from '@/api/post-manage'
export default {
  name: "PostFormView",
  components: {
    BForm,
    BFormGroup,
    BFormInput,
    BFormSelect,
    BFormTextarea,
    BButton,
    BFormInvalidFeedback
  },
  data() {
    return {
      form: {
        title: "",
        category: "分類一",
        content: "",
      },
      validation:{
        title:{
          state: null,
          message: ''
        },
        category:{
          state: null,
          message: ''
        },
        content:{
          state: null,
          message: ''
        }
      },
      category: ["分類一", "分類二", "分類三"],
    };
  },
  computed:{
    action: function(){
      if(this.$route.params.id == undefined){
        return 'CREATE'
      }else{
        return 'UPDATE'
      }
    }
  },
  mounted(){
    if(this.action == 'UPDATE'){
      fetchPostDetail(this.$route.params.id).then(res =>{
        this.form.category = res.data.category
        this.form.title = res.data.title
        this.form.content = res.data.content
      })
    }
  },
  methods:{
    submit: function(){
      if(this.action == 'CREATE'){
        createPost(
          this.form.category.trim(), 
          this.form.title.trim(),
          this.form.content.trim()
        ).then(res => {
          alert(res.message)
          this.$router.push('/')
        }).catch(error => {
          const errors = error.response.data
          const mapping ={
            'createPost.form.title': 'title',
            'createPost.form.category': 'category',
            'createPost.form.content': 'content'
          }
          this.handleError(errors, mapping)
        })
      }else{
        updatePost(
          this.$route.params.id,
          this.form.category.trim(),
          this.form.title.trim(),
          this.form.content.trim()
        ).then(res => {
          alert(res.message)
          this.$router.push('/')
        }).catch(error => {
          const errors = error.response.data
          const mapping ={
            'updateSelfPost.form.title': 'title',
            'updateSelfPost.form.category': 'category',
            'updateSelfPost.form.content': 'content'
          }
          this.handleError(errors, mapping)
        })
      }
    },
    handleError: function(errors, mapping){
      const validation = {
        title:{
          state: true,
          message: ''
        },
        category:{
          state: true,
          message: ''
        },
        content:{
          state: true,
          message: ''
        }
      }
      errors.violations.forEach(data =>{
        const key = mapping[data.field]
        validation[key].message = data.message
        validation[key].state = false
      })
      this.validation = validation
    }
  }
};
</script>