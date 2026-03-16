# PRM User Business Detail Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a user business-detail page under the organization domain so clicking a team member’s name opens a `日程 / 需求 / Bug` view.

**Architecture:** Keep the organization domain shell unchanged, but add a new deep route under the `团队` tab. Reuse existing `task`, `requirement`, and `bug` APIs, filter by user where supported, and present the data in a dedicated user-centric page.

**Tech Stack:** Vue 3 SFC, TypeScript, Vue Router, Element Plus, existing Axios API wrappers, `vue-tsc`.

