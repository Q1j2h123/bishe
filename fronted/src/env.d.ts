/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 添加vue-monaco模块声明
declare module 'vue-monaco';

// 添加monaco-editor/react模块声明
declare module '@monaco-editor/react';

// 添加md-editor-v3模块声明
declare module 'md-editor-v3'; 