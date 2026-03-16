# PRM Admin People Management Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Refactor the admin domain into a single People Management work area with `部门 / 用户 / 权限` pages.

**Architecture:** Keep the existing blue domain shell, but change the admin tabs from `系统 / 动态 / 公司` to `部门 / 用户 / 权限`. Implement three focused admin pages under `/admin/*`, reusing existing user and role data where possible and matching the provided screenshot structure.

**Tech Stack:** Vue 3 SFC, TypeScript, Vue Router, Element Plus, Axios wrappers, `vue-tsc`.

