<template>
  <div class="knowledge-container">
    <div class="header">
      <h1 class="title">知识库管理</h1>
      <p class="subtitle">文档上传与知识库同步</p>
    </div>

    <div class="content">
      <div class="action-panel">
        <div class="upload-section">
          <h3>📤 文档上传</h3>
          <div 
            class="upload-area"
            :class="{ 'drag-over': isDragOver }"
            @dragover.prevent="isDragOver = true"
            @dragleave.prevent="isDragOver = false"
            @drop.prevent="handleDrop"
          >
            <div class="upload-icon">📁</div>
            <p>拖拽文件到此处，或点击选择</p>
            <p class="upload-hint">支持 PDF、Word、Excel、MD 格式</p>
            <input 
              type="file" 
              ref="fileInput"
              @change="handleFileSelect" 
              accept=".pdf,.docx,.xlsx,.md"
              multiple
              style="display: none"
            >
            <button class="btn-select" @click="$refs.fileInput.click()">选择文件</button>
          </div>
          
          <div v-if="selectedFiles.length > 0" class="selected-files">
            <div v-for="(file, index) in selectedFiles" :key="index" class="file-item">
              <span class="file-icon">📄</span>
              <span class="file-name">{{ file.name }}</span>
              <span class="file-size">{{ formatFileSize(file.size) }}</span>
              <button class="btn-remove" @click="removeFile(index)">✕</button>
            </div>
            <button 
              class="btn-upload" 
              @click="uploadFiles"
              :disabled="uploading"
            >
              {{ uploading ? '上传中...' : '上传文档' }}
            </button>
          </div>

          <div v-if="uploadResult" class="upload-result" :class="uploadResult.success ? 'success' : 'error'">
            {{ uploadResult.message }}
          </div>
        </div>

        <div class="sync-section">
          <h3>🔄 同步本地目录</h3>
          <p class="sync-path">知识库目录: {{ basePath }}</p>
          <button 
            class="btn-sync" 
            @click="syncDirectory"
            :disabled="syncing"
          >
            {{ syncing ? '同步中...' : '同步目录' }}
          </button>
          <div v-if="syncResult" class="sync-result" :class="syncResult.success ? 'success' : 'error'">
            {{ syncResult.message }} ({{ syncResult.count }} 个文档)
          </div>
        </div>
      </div>

      <div class="document-list">
        <h3>📚 知识库文档</h3>
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="documents.length === 0" class="empty">
          暂无文档，请上传或同步
        </div>
        <div v-else class="table-container">
          <table>
            <thead>
              <tr>
                <th>文件名</th>
                <th>类型</th>
                <th>Chunk数</th>
                <th>导入时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="doc in documents" :key="doc.id">
                <td>
                  <a
                    href="javascript:void(0)"
                    class="file-link"
                    @click="previewDocument(doc.id, doc.fileName)"
                    :title="doc.fileName"
                  >
                    <span class="file-icon">{{ getFileIcon(doc.fileType) }}</span>
                    <span class="file-name-text">{{ doc.fileName }}</span>
                  </a>
                </td>
                <td><span class="badge">{{ doc.fileType }}</span></td>
                <td>{{ doc.chunkCount }}</td>
                <td>{{ formatDate(doc.createdAt) }}</td>
                <td>
                  <button class="btn-preview" @click="previewDocument(doc.id, doc.fileName)">预览</button>
                  <button class="btn-download" @click="downloadDocument(doc.id, doc.fileName)">下载</button>
                  <button class="btn-delete" @click="deleteDocument(doc.id)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
          
          <div class="pagination">
            <button 
              class="btn-page" 
              @click="loadPage(currentPage - 1)"
              :disabled="currentPage === 0"
            >
              上一页
            </button>
            <span class="page-info">
              第 {{ currentPage + 1 }} / {{ totalPages }} 页 (共 {{ total }} 条)
            </span>
            <button 
              class="btn-page" 
              @click="loadPage(currentPage + 1)"
              :disabled="currentPage >= totalPages - 1"
            >
              下一页
            </button>
          </div>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AppFooter from '../components/AppFooter.vue'

const API_BASE = 'http://localhost:8123/api'

const documents = ref([])
const loading = ref(false)
const uploading = ref(false)
const syncing = ref(false)
const isDragOver = ref(false)
const selectedFiles = ref([])
const uploadResult = ref(null)
const syncResult = ref(null)
const basePath = ref('')
const currentPage = ref(0)
const totalPages = ref(0)
const total = ref(0)
const pageSize = ref(20)

const fileInput = ref(null)

onMounted(() => {
  loadDocuments()
  loadConfig()
})

async function loadDocuments() {
  loading.value = true
  try {
    const response = await fetch(`${API_BASE}/knowledge/list?page=${currentPage.value}&size=${pageSize.value}`)
    const data = await response.json()
    if (data.success) {
      documents.value = data.data || []
      total.value = data.total || 0
      totalPages.value = data.totalPages || 0
    }
  } catch (error) {
    console.error('加载文档列表失败:', error)
  } finally {
    loading.value = false
  }
}

function loadPage(page) {
  currentPage.value = page
  loadDocuments()
}

async function loadConfig() {
  try {
    const response = await fetch(`${API_BASE}/knowledge/config`)
    const data = await response.json()
    if (data.success) {
      basePath.value = data.basePath || '未配置'
    }
  } catch (error) {
    console.error('加载配置失败:', error)
    basePath.value = '加载失败'
  }
}

function handleFileSelect(event) {
  const files = Array.from(event.target.files)
  selectedFiles.value = [...selectedFiles.value, ...files]
  event.target.value = ''
}

function handleDrop(event) {
  isDragOver.value = false
  const files = Array.from(event.dataTransfer.files)
  selectedFiles.value = [...selectedFiles.value, ...files]
}

function removeFile(index) {
  selectedFiles.value.splice(index, 1)
}

async function uploadFiles() {
  if (selectedFiles.value.length === 0) return
  
  uploading.value = true
  uploadResult.value = null
  
  for (const file of selectedFiles.value) {
    const formData = new FormData()
    formData.append('file', file)
    
    try {
      const response = await fetch(`${API_BASE}/knowledge/upload`, {
        method: 'POST',
        body: formData
      })
      const data = await response.json()
      uploadResult.value = data
    } catch (error) {
      uploadResult.value = { success: false, message: `上传失败: ${error.message}` }
    }
  }
  
  selectedFiles.value = []
  uploading.value = false
  loadDocuments()
}

async function syncDirectory() {
  syncing.value = true
  syncResult.value = null
  
  try {
    const response = await fetch(`${API_BASE}/knowledge/sync`, {
      method: 'POST'
    })
    const data = await response.json()
    syncResult.value = data
  } catch (error) {
    syncResult.value = { success: false, message: `同步失败: ${error.message}` }
  } finally {
    syncing.value = false
    loadDocuments()
  }
}

async function deleteDocument(id) {
  if (!confirm('确定要删除这个文档吗？')) return
  
  try {
    const response = await fetch(`${API_BASE}/knowledge/${id}`, {
      method: 'DELETE'
    })
    const data = await response.json()
    if (data.success) {
      loadDocuments()
    } else {
      alert(data.message || '删除失败')
    }
  } catch (error) {
    alert(`删除失败: ${error.message}`)
  }
}

async function previewDocument(id, fileName) {
  try {
    const response = await fetch(`${API_BASE}/knowledge/${id}/preview`)
    const html = await response.text()

    const previewWindow = window.open('', '_blank', 'width=1000,height=800,left=100,top=50')
    if (previewWindow) {
      previewWindow.document.write(html)
      previewWindow.document.close()
      previewWindow.document.title = fileName + ' - 预览'
    }
  } catch (error) {
    alert(`预览失败: ${error.message}`)
  }
}

async function downloadDocument(id, fileName) {
  try {
    const response = await fetch(`${API_BASE}/knowledge/${id}/download`)
    if (!response.ok) {
      throw new Error(`下载失败: ${response.status} ${response.statusText}`)
    }
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = fileName || `document-${id}`
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
  } catch (error) {
    alert(`下载失败: ${error.message}`)
  }
}

function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString()
}

function getFileIcon(fileType) {
  switch (fileType?.toLowerCase()) {
    case 'pdf':
      return '📕'
    case 'docx':
      return '📝'
    case 'xlsx':
      return '📊'
    case 'md':
      return '📖'
    default:
      return '📄'
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@400;500;700&display=swap');

.knowledge-container {
  min-height: 100vh;
  background-color: #111122;
  background-image: 
    linear-gradient(0deg, rgba(8, 17, 34, 0.9), rgba(5, 8, 20, 0.9)),
    url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"><rect x="0" y="0" width="100" height="1" fill="%23111133" opacity="0.3"/><rect x="0" y="0" width="1" height="100" fill="%23111133" opacity="0.3"/></svg>');
  background-size: auto, 40px 40px;
  padding-bottom: 80px;
}

.header {
  padding: 50px 20px 30px;
  text-align: center;
  background: linear-gradient(180deg, rgba(0, 240, 255, 0.1) 0%, transparent 100%);
}

.title {
  font-family: 'Orbitron', sans-serif;
  font-size: 2.5rem;
  color: #edf7ff;
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.5);
  margin-bottom: 10px;
}

.subtitle {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.6);
}

.content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.action-panel {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
}

.upload-section,
.sync-section {
  background: rgba(17, 23, 41, 0.8);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(0, 240, 255, 0.2);
}

.upload-section h3,
.sync-section h3,
.document-list h3 {
  font-family: 'Orbitron', sans-serif;
  color: #edf7ff;
  margin-bottom: 15px;
  font-size: 1.2rem;
}

.upload-area {
  border: 2px dashed rgba(0, 240, 255, 0.3);
  border-radius: 12px;
  padding: 30px;
  text-align: center;
  transition: all 0.3s;
  cursor: pointer;
}

.upload-area.drag-over {
  border-color: #00f0ff;
  background: rgba(0, 240, 255, 0.1);
}

.upload-icon {
  font-size: 3rem;
  margin-bottom: 10px;
}

.upload-hint {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 5px;
}

.btn-select {
  margin-top: 15px;
  padding: 10px 25px;
  background: linear-gradient(90deg, #0088ff, #00b2ff);
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 500;
}

.btn-select:hover {
  box-shadow: 0 0 15px rgba(0, 178, 255, 0.7);
}

.selected-files {
  margin-top: 15px;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 10px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  margin-bottom: 8px;
}

.file-icon {
  margin-right: 10px;
}

.file-name {
  flex: 1;
  color: #edf7ff;
  font-size: 0.9rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.8rem;
  margin: 0 15px;
}

.btn-remove {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  font-size: 1rem;
}

.btn-remove:hover {
  color: #ff4444;
}

.btn-upload {
  width: 100%;
  margin-top: 10px;
  padding: 12px;
  background: linear-gradient(90deg, #00cc6a, #00ff88);
  color: #111;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.btn-upload:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.sync-path {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.9rem;
  margin-bottom: 15px;
  word-break: break-all;
}

.btn-sync {
  width: 100%;
  padding: 12px;
  background: linear-gradient(90deg, #ff8800, #ffaa00);
  color: #111;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.btn-sync:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.upload-result,
.sync-result {
  margin-top: 15px;
  padding: 10px;
  border-radius: 8px;
  font-size: 0.9rem;
}

.upload-result.success,
.sync-result.success {
  background: rgba(0, 255, 136, 0.2);
  color: #00ff88;
}

.upload-result.error,
.sync-result.error {
  background: rgba(255, 68, 68, 0.2);
  color: #ff4444;
}

.document-list {
  background: rgba(17, 23, 41, 0.8);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(0, 240, 255, 0.2);
}

.document-list h3 {
  font-family: 'Orbitron', sans-serif;
  color: #edf7ff;
  margin-bottom: 15px;
}

.loading,
.empty {
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
  padding: 40px;
}

.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead tr {
  background: rgba(0, 240, 255, 0.1);
}

th {
  padding: 12px;
  text-align: left;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
  font-size: 0.9rem;
}

td {
  padding: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

.file-link {
  color: #00f0ff;
  text-decoration: none;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  max-width: 250px;
  padding: 4px 8px;
  margin: -4px -8px;
  border-radius: 4px;
  transition: all 0.3s ease;
  position: relative;
}

.file-icon {
  margin-right: 6px;
  font-size: 0.9em;
  flex-shrink: 0;
}

.file-name-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
}

.file-link:hover {
  text-decoration: none;
  background: rgba(0, 240, 255, 0.25);
  color: #00ffff;
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.5);
  transform: translateX(3px);
}

.file-link:active {
  transform: translateX(1px);
}

.badge {
  display: inline-block;
  padding: 3px 10px;
  background: rgba(0, 240, 255, 0.2);
  color: #00f0ff;
  border-radius: 12px;
  font-size: 0.8rem;
  text-transform: uppercase;
}

.btn-preview {
  padding: 6px 15px;
  background: rgba(0, 178, 255, 0.2);
  color: #00b2ff;
  border: 1px solid rgba(0, 178, 255, 0.3);
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.8rem;
  margin-right: 8px;
}

.btn-preview:hover {
  background: rgba(0, 178, 255, 0.4);
}

.btn-download {
  padding: 6px 15px;
  background: rgba(0, 255, 136, 0.2);
  color: #00ff88;
  border: 1px solid rgba(0, 255, 136, 0.3);
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.8rem;
  margin-right: 8px;
}

.btn-download:hover {
  background: rgba(0, 255, 136, 0.4);
}

.btn-delete {
  padding: 6px 15px;
  background: rgba(255, 68, 68, 0.2);
  color: #ff4444;
  border: 1px solid rgba(255, 68, 68, 0.3);
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.8rem;
}

.btn-delete:hover {
  background: rgba(255, 68, 68, 0.4);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  gap: 15px;
}

.btn-page {
  padding: 8px 20px;
  background: linear-gradient(90deg, #0088ff, #00b2ff);
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
}

.btn-page:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .action-panel {
    grid-template-columns: 1fr;
  }
  
  .title {
    font-size: 2rem;
  }
}
</style>
