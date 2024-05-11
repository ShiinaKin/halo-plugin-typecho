<script setup lang="ts">
import "@/style.css";
import { VButton, Toast } from "@halo-dev/components";
import { ref } from "vue";

const uploadFile = ref();
const loading = ref(false);

const handleFileChange = (event: any) => {
  const file = event.target.files[0];
  const allowedExtensions = /(\.zip|\.tar|\.dump)$/i;

  if (!allowedExtensions.exec(file.name)) {
    Toast.error("仅支持 .zip .tar .dump 类型的文件", { duration: 5000 });
    event.target.value = "";
    uploadFile.value = null;
    return;
  }

  uploadFile.value = file;
};

function handleUpload() {
  if (!uploadFile.value) {
    Toast.error("请选择文件", { duration: 5000 });
    return;
  }
  loading.value = true;
  const formData = new FormData();
  formData.append("file", uploadFile.value);
  fetch(
    `${window.location.origin}/apis/io.sakurasou.halo.typecho/v1/typecho/upload`,
    {
      method: "POST",
      body: formData,
    }
  )
    .then((res) => {
      loading.value = false;
      res.json().then((data) => {
        if (data.success) {
          const resultMsg = data.data;
          resultMsg.split("\n").forEach((msg: string) => {
            Toast.success(msg.trim(), { duration: 7500 });
          });
        } else {
          Toast.error(data.msg, { duration: 5000 });
        }
      });
    })
    .catch((error) => {
      console.error(error);
    });
}
</script>

<template>
  <div class="relative p-10 h-full bg-white">
    <div
      v-if="loading"
      class="absolute inset-0 bg-gray-600 opacity-50 flex items-center justify-center gap-2"
    >
      <svg
        class="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
      >
        <circle
          class="opacity-25"
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          stroke-width="4"
        ></circle>
        <path
          class="opacity-75"
          fill="currentColor"
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
        ></path>
      </svg>
      <div>处理中, 请等待请求返回...</div>
    </div>
    <div>
      <div class="text-2xl py-5">Typecho迁移</div>
      <div class="h-0 w-full border border-gray-500"></div>
      <div class="flex flex-col gap-3">
        <div class="pt-5 flex flex-col gap-4">
          <div>
            Step1. 在Typecho安装
            <a
              class="text-blue-500 underline"
              href="https://github.com/mashirot/Typecho-Plugin-Tp2MD"
              >导出插件</a
            >
          </div>
          <div>
            Step2. 下载导出的文件, 见
            <a
              class="text-blue-500 underline"
              href="https://github.com/mashirot/Typecho-Plugin-Tp2MD#README"
              >Plugin README</a
            >
          </div>
          <div class="flex flex-col gap-2">
            <div>Step3. 上传导出的文件</div>
            <div class="flex flex-row content-center gap-4">
              <input
                class="h-9 border border-gray-500 rounded"
                type="file"
                placeholder="选择文件"
                @change="handleFileChange"
              />
              <VButton @click="handleUpload">上传</VButton>
            </div>
            <div class="text-sm text-gray-500">
              <div>Tips: 仅支持 .zip .tar .dump 类型的文件</div>
              <div>Tips: 耗时较长, 请等待请求返回</div>
            </div>
          </div>
        </div>
        <div
          class="p-2 flex flex-col bg-yellow-200 border border-orange-600 rounded"
        >
          <div class="text-lg">注意：</div>
          <div>
            1. 请在本插件的设置内填写Token, 可在 个人中心-个人令牌 生成,
            必要权限: 文章管理, 页面管理
          </div>
          <div>
            2. 由于Halo目前没有很好的办法判断文章的slug是否重复, 请不要重复导入
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
