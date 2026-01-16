import js from '@eslint/js'
import typescriptEslint from 'typescript-eslint'
import pluginVue from 'eslint-plugin-vue'
import eslintConfigPrettier from 'eslint-config-prettier'
import * as parserVue from 'vue-eslint-parser'

export default [
  // 基础 JavaScript 规则
  js.configs.recommended,

  // Vue 3 规则
  ...pluginVue.configs['flat/recommended'],

  // TypeScript 规则
  ...typescriptEslint.configs.recommended,

  // Prettier 配置（放在最后以覆盖冲突规则）
  eslintConfigPrettier,

  {
    files: ['**/*.vue'],
    languageOptions: {
      parser: parserVue,
      parserOptions: {
        parser: typescriptEslint.parser,
        ecmaVersion: 'latest',
        sourceType: 'module'
      }
    }
  },

  {
    files: ['**/*.ts', '**/*.tsx'],
    languageOptions: {
      parser: typescriptEslint.parser,
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module'
      },
      globals: {
        window: 'readonly',
        document: 'readonly',
        console: 'readonly',
        setTimeout: 'readonly',
        setInterval: 'readonly',
        clearTimeout: 'readonly',
        clearInterval: 'readonly',
        URL: 'readonly',
        Blob: 'readonly',
        HTMLElement: 'readonly',
        navigator: 'readonly'
      }
    }
  },

  {
    files: ['**/*.vue'],
    languageOptions: {
      parser: parserVue,
      parserOptions: {
        parser: typescriptEslint.parser,
        ecmaVersion: 'latest',
        sourceType: 'module'
      },
      globals: {
        window: 'readonly',
        document: 'readonly',
        console: 'readonly',
        setTimeout: 'readonly',
        setInterval: 'readonly',
        clearTimeout: 'readonly',
        clearInterval: 'readonly',
        URL: 'readonly',
        Blob: 'readonly',
        HTMLElement: 'readonly',
        navigator: 'readonly'
      }
    }
  },

  {
    rules: {
      // Vue 相关规则
      'vue/multi-word-component-names': 'off',
      'vue/no-v-html': 'warn',
      'vue/require-default-prop': 'off',
      'vue/require-explicit-emits': 'off',
      'vue/html-self-closing': 'off', // 关闭自闭合标签要求
      'vue/attributes-order': 'off', // 关闭属性顺序要求

      // TypeScript 相关规则
      '@typescript-eslint/no-unused-vars': [
        'warn',
        {
          argsIgnorePattern: '^_',
          varsIgnorePattern: '^_'
        }
      ],
      '@typescript-eslint/no-explicit-any': 'warn',
      '@typescript-eslint/explicit-module-boundary-types': 'off',
      '@typescript-eslint/no-non-null-assertion': 'warn',

      // 通用 JavaScript 规则
      'no-console': 'off', // 关闭 console 限制
      'no-debugger': 'warn',
      'no-unused-vars': 'off', // 使用 TypeScript 版本
      'prefer-const': 'warn',
      'no-var': 'error',
      'eqeqeq': ['warn', 'always'],
      'curly': 'off', // 关闭大括号要求
      'no-throw-literal': 'warn',
      'prefer-promise-reject-errors': 'warn',
      'no-undef': 'off' // 关闭未定义变量检查（TypeScript 已处理）
    }
  },

  {
    ignores: [
      'node_modules/**',
      'dist/**',
      'build/**',
      '*.config.js',
      '*.config.ts'
    ]
  }
]
