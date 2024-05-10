# plugin-typecho

Halo2.0的Typecho文章迁移插件，导入由 [Typecho-Plugin-Tp2MD](https://github.com/mashirot/Typecho-Plugin-Tp2MD) 生成的文件

## 使用步骤

1. 安装插件
2. 在 `个人中心-个人令牌` 生成PAT令牌, 必要权限:`文章管理`, `页面管理`
3. 在插件的设置中填入PAT令牌
4. 在Typecho安装对应插件并导出文件
5. 在 `Typecho迁移` 菜单内上传文件，并等待Message通知结果

> 注意：
> 1. 不要重复导入文章，`上传`按钮没有做防抖，点击一次等待结果即可