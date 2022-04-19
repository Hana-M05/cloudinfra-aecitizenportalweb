{{/*
Expand the name of the chart.
*/}}
{{- define "loginservice.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "loginservice.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/* 
Create configmap name 
*/}}
{{- define "loginservice.configmap" }}
{{- if .Values.configmap.name }}
{{- .Values.configmap.name }}
{{- else }}
{{- printf "%s-cm" .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{/* 
Create secrets 
*/}}
{{- define "loginservice.secrets" }}
{{- if .Values.secretdtls.name }}
{{- .Values.secretdtls.name }}
{{- else }}
{{- printf "%s-secrets" .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{/*
Create Namespace 
*/}}
{{- define "loginservice.namespace" }}
{{- if .Release.Namespace }}
{{- .Release.Namespace }}
{{- else }}
{{- .Values.namespace}}
{{- end }}
{{- end }}
{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "loginservice.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "loginservice.labels" -}}
helm.sh/chart: {{ include "loginservice.chart" . }}
{{ include "loginservice.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}

{{- end }}

{{/*
Selector labels
*/}}
{{- define "loginservice.selectorLabels" -}}
app.kubernetes.io/name: {{ include "loginservice.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "loginservice.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "loginservice.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Expand the name of the chart.
*/}}
{{- define "loginservice.clusterrole" -}}
{{- printf "%s-%s" .Release.Name .Release.Namespace | trunc 63 | trimSuffix "-" }}
{{- end }}


{{/*
Expand the name of the chart.
*/}}
{{- define "loginservice.clusterrolebinding" -}}
{{- printf "%s-%s" .Release.Name .Release.Namespace | trunc 63 | trimSuffix "-" }}
{{- end }}
